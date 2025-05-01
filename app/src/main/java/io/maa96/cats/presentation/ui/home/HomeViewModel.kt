package io.maa96.cats.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.usecase.GetCatBreedsUseCase
import io.maa96.cats.domain.usecase.SearchBreedsUseCase
import io.maa96.cats.domain.usecase.UpdateFavoriteStatusUseCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"
private const val PAGE_SIZE = 10

/**
 * ViewModel for the Home screen that manages cat breed data, search functionality,
 * favorites filtering, and pagination.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCatBreedsUseCase: GetCatBreedsUseCase,
    private val searchBreedsUseCase: SearchBreedsUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    private val searchDebouncer: SearchDebouncer
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        setupSearchDebouncing()
        loadBreeds()
    }

    // ----- Public API -----

    /**
     * Handle UI events from the Home screen
     */
    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnSearchQueryChange -> updateSearchQuery(event.query)
            HomeScreenEvent.ShowFavorites -> toggleFavoritesFilter()
            HomeScreenEvent.Refresh -> refreshData()
            is HomeScreenEvent.ToggleFavorite -> toggleFavorite(event.breed)
            HomeScreenEvent.ToggleFilterDialog -> TODO()
            HomeScreenEvent.ToggleTheme -> TODO()
            HomeScreenEvent.LoadMoreBreeds -> loadMoreBreeds()
            HomeScreenEvent.ClearError -> clearError()
        }
    }

    // ----- Search Functionality -----

    private fun setupSearchDebouncing() {
        viewModelScope.launch {
            searchDebouncer.getQueryFlow()
                .collect { query ->
                    if (query.isNotBlank()) {
                        performSearch(query)
                    } else {
                        clearSearchResults()
                        loadBreeds()
                    }
                }
        }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchDebouncer.setQuery(query)
    }

    private suspend fun performSearch(query: String) {
        resetPagination()

        searchBreedsUseCase(query, 0)
            .onStart { setLoading(true) }
            .catch { error ->
                Log.e(TAG, "Search error: ${error.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
            .collect { result ->
                updateUiState(result, isInitialLoad = true)
            }
    }

    private fun clearSearchResults() {
        _uiState.update { currentState ->
            currentState.copy(
                breeds = emptyList(),
                filteredBreeds = emptyList(),
                isLoading = false,
                showingFavoritesOnly = currentState.showingFavoritesOnly
            )
        }
    }

    // ----- Data Loading -----

    private fun loadBreeds() {
        viewModelScope.launch {
            val page = 1

            getCatBreedsUseCase(limit = PAGE_SIZE, page = page)
                .onStart { setLoading(true) }
                .catch { error ->
                    Log.e(TAG, "Load breeds error: ${error.message}")
                    _uiState.update { it.copy(isLoading = false) }
                }.collect { result ->
                    updateUiState(result, isInitialLoad = true)
                    _uiState.update { it.copy(currentPage = page) }
                }
        }
    }

    private fun loadMoreBreeds() {
        // Skip if already loading or no more data available
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore || !_uiState.value.hasMoreData) {
            return
        }

        viewModelScope.launch {
            val currentState = _uiState.value
            val nextPage = currentState.currentPage + 1

            _uiState.update { it.copy(isLoadingMore = true) }

            getCatBreedsUseCase(limit = PAGE_SIZE, page = nextPage)
                .catch { error ->
                    Log.e(TAG, "Load more breeds error: ${error.message}")
                    _uiState.update { it.copy(isLoadingMore = false) }
                }.collect { result ->
                    handlePaginationResult(result, nextPage)
                }
        }
    }

    private fun handlePaginationResult(result: Resource<List<Cat>>, nextPage: Int) {
        when (result) {
            is Resource.Success -> {
                val newBreeds = result.data ?: emptyList()
                if (newBreeds.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            hasMoreData = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            breeds = newBreeds,
                            isLoadingMore = false,
                            currentPage = nextPage,
                            error = null,
                            isStale = false,
                            lastUpdated = getCurrentDateTime()
                        )
                    }
                }
            }
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = result.message
                    )
                }
            }
            is Resource.Loading -> { /* Already handled by setting isLoadingMore = true */ }
        }
    }

    private fun refreshData() {
        resetPagination()
        loadBreeds()
    }

    private fun resetPagination() {
        _uiState.update { it.copy(currentPage = 0, hasMoreData = true) }
    }

    // ----- Favorites Management -----

    private fun toggleFavoritesFilter() {
        val currentState = _uiState.value
        val showingFavoritesOnly = !currentState.showingFavoritesOnly

        if (showingFavoritesOnly) {
            // Filter to show only favorites
            val favoriteBreeds = currentState.breeds.filter { it.isFavorite }
            _uiState.update {
                it.copy(
                    filteredBreeds = favoriteBreeds,
                    showingFavoritesOnly = true
                )
            }
            Log.d(TAG, "Showing ${favoriteBreeds.size} favorite breeds")
        } else {
            // Show all breeds again
            _uiState.update {
                it.copy(
                    filteredBreeds = emptyList(),
                    showingFavoritesOnly = false
                )
            }
            Log.d(TAG, "Showing all breeds")
        }
    }

    private fun toggleFavorite(breed: Cat) {
        // Optimistically update UI state
        updateBreedInState(breed)

        // Persist the change
        viewModelScope.launch {
            updateFavoriteStatusUseCase(breed.id, breed.isFavorite.not())
                .catch { error ->
                    Log.e(TAG, "Toggle favorite error: ${error.message}")
//                    revertFavoriteStatus(breed.id, originalIsFavorite)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Error -> {
                            Log.e(TAG, "Toggle favorite failed: ${result.message}")
//                            revertFavoriteStatus(breed.id, originalIsFavorite)
                        }
                        is Resource.Success -> {
                            Log.d(TAG, "Toggle favorite success for breed ${breed.id}")
                        }
                        is Resource.Loading -> { /* Handled by optimistic update */ }
                    }
                }
        }
    }

    private fun updateBreedInState(breed: Cat) {
        _uiState.update { currentState ->
            // Update in main list
            val updatedList = currentState.breeds.toMutableList()
            val index = updatedList.indexOfFirst { it.id == breed.id }
            if (index != -1) {
                updatedList[index] = breed.copy(isFavorite = breed.isFavorite.not())
            }

            // Update filtered list if needed
            val updatedFilteredList = updateFilteredList(currentState, breed)

            currentState.copy(
                breeds = updatedList,
                filteredBreeds = updatedFilteredList
            )
        }
    }

    private fun updateFilteredList(
        currentState: HomeScreenState,
        breed: Cat
    ): List<Cat> {
        if (!currentState.showingFavoritesOnly) {
            return currentState.filteredBreeds
        }

        return if (breed.isFavorite) {
            // Add to filtered list if not already there
            if (currentState.filteredBreeds.none { it.id == breed.id }) {
                currentState.filteredBreeds + breed
            } else {
                // Update existing item
                currentState.filteredBreeds.map {
                    if (it.id == breed.id) breed else it
                }
            }
        } else {
            // Remove from filtered list
            currentState.filteredBreeds.filter { it.id != breed.id }
        }
    }

    private fun revertFavoriteStatus(breedId: String, originalIsFavorite: Boolean) {
        _uiState.update { currentState ->
            // Find and update the breed in the main list
            val updatedList = currentState.breeds.toMutableList()
            val index = updatedList.indexOfFirst { it.id == breedId }
            var updatedBreed: Cat? = null

            if (index != -1) {
                updatedBreed = updatedList[index].copy(isFavorite = originalIsFavorite)
                updatedList[index] = updatedBreed
            }

            // Update filtered list if needed
            val updatedFilteredList = if (updatedBreed != null) {
                updateFilteredList(currentState, updatedBreed)
            } else {
                currentState.filteredBreeds
            }

            currentState.copy(
                breeds = updatedList,
                filteredBreeds = updatedFilteredList
            )
        }
    }

    // ----- UI State Management -----

    private fun updateUiState(result: Resource<List<Cat>>, isInitialLoad: Boolean = false) {
        when (result) {
            is Resource.Error -> handleErrorResult(result, isInitialLoad)
            is Resource.Loading -> handleLoadingState(isInitialLoad)
            is Resource.Success -> handleSuccessResult(result, isInitialLoad)
        }
    }

    private fun handleErrorResult(result: Resource<List<Cat>>, isInitialLoad: Boolean) {
        // Check if we have data to show even with an error
        if (result.data.isNullOrEmpty().not()) {
            _uiState.update { currentState ->
                currentState.copy(
                    breeds = if (isInitialLoad) result.data ?: listOf() else currentState.breeds,
                    isLoading = false,
                    isLoadingMore = false,
                    error = result.message,
                    isStale = true
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = result.message
                )
            }
        }
    }

    private fun handleLoadingState(isInitialLoad: Boolean) {
        if (isInitialLoad) {
            _uiState.update { it.copy(isLoading = true) }
        } else {
            _uiState.update { it.copy(isLoadingMore = true) }
        }
    }

    private fun handleSuccessResult(result: Resource<List<Cat>>, isInitialLoad: Boolean) {
        val newData = result.data ?: listOf()
        _uiState.update { currentState ->
            // Process the data to preserve favorite status
            val processedData = processNewData(currentState, newData, isInitialLoad)

            // Update filtered list if showing favorites only
            val updatedFilteredList = if (currentState.showingFavoritesOnly) {
                processedData.filter { it.isFavorite }
            } else {
                currentState.filteredBreeds
            }

            currentState.copy(
                breeds = processedData,
                filteredBreeds = updatedFilteredList,
                isLoading = false,
                isLoadingMore = false,
                error = null,
                isStale = false,
                hasMoreData = newData.isNotEmpty(),
                lastUpdated = getCurrentDateTime(),
                showingFavoritesOnly = currentState.showingFavoritesOnly
            )
        }
    }

    private fun processNewData(
        currentState: HomeScreenState,
        newData: List<Cat>,
        isInitialLoad: Boolean
    ): List<Cat> {
        // If loading initial data and we already have breeds with favorite status
        if (isInitialLoad && currentState.breeds.isNotEmpty()) {
            // Create a map of existing breeds by ID for quick lookup
            val existingBreedsMap = currentState.breeds.associateBy { it.id }

            // For each new breed, preserve its favorite status if it exists in our current list
            return newData.map { newBreed ->
                existingBreedsMap[newBreed.id]?.let { existingBreed ->
                    // Keep the favorite status from the existing breed
                    newBreed.copy(isFavorite = existingBreed.isFavorite)
                } ?: newBreed // Use new breed as is if not in our current list
            }
        } else if (!isInitialLoad && newData.isNotEmpty()) {
            // For pagination (loading more), preserve existing breeds and add new ones
            val existingBreedsMap = currentState.breeds.associateBy { it.id }
            val newBreedsList = currentState.breeds.toMutableList()

            // Add only new breeds that aren't already in the list
            newBreedsList.addAll(newData.filter { newBreed -> !existingBreedsMap.containsKey(newBreed.id) })
            return newBreedsList
        } else {
            // Just use the new data as is
            return newData
        }
    }

    // ----- Utility Methods -----

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }
}
