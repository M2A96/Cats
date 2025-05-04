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
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    init {
        setupSearchDebouncing()
        loadInitialBreeds()
    }

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnSearchQueryChange -> updateSearchQuery(event.query)
            HomeScreenEvent.ShowFavorites -> toggleFavoritesFilter()
            HomeScreenEvent.Refresh -> refreshData()
            is HomeScreenEvent.ToggleFavorite -> toggleFavorite(event.breedId, event.isFavorite)
            HomeScreenEvent.ToggleTheme -> toggleTheme()
            HomeScreenEvent.LoadMoreBreeds -> loadMoreBreeds()
            HomeScreenEvent.ClearError -> clearError()
        }.also { Log.d(TAG, "Processed event: $event") }
    }

    private fun setupSearchDebouncing() {
        viewModelScope.launch(Dispatchers.Main) {
            searchDebouncer.getQueryFlow().collect { query ->
                Log.d(TAG, "SearchDebouncer emitted query: $query")
                updateState { it.copy(searchQuery = query) }
                if (query.isNotBlank()) {
                    performSearch(query)
                } else {
                    clearSearchResults()
                    loadInitialBreeds()
                }
            }
        }
    }

    private fun loadInitialBreeds() {
        loadBreeds(page = 1, isInitialLoad = true)
    }

    private fun loadMoreBreeds() {
        val currentPage = _uiState.value.currentPage
        Log.d(TAG, "loadMoreBreeds: Triggering load for page ${currentPage + 1}")
        loadBreeds(page = currentPage + 1, isInitialLoad = false)
    }

    private fun loadBreeds(page: Int, isInitialLoad: Boolean) {
        if (shouldSkipLoad(isInitialLoad)) {
            Log.d(TAG, "Skipping loadBreeds: already loaded or loading")
            return
        }
        viewModelScope.launch {
            getCatBreedsUseCase(limit = PAGE_SIZE, page = page)
                .onStart { updateState { it.copy(isLoading = isInitialLoad, isLoadingMore = !isInitialLoad) } }
                .catch { error ->
                    Log.e(TAG, "Load breeds error: ${error.message}")
                    updateState { it.copy(isLoading = false, isLoadingMore = false) }
                }
                .collect { result ->
                    Log.d(TAG, "Collected result for page $page: $result")
                    handleBreedsResult(result, page, isInitialLoad)
                }
        }
    }

    private fun handleBreedsResult(result: Resource<List<Cat>>, page: Int, isInitialLoad: Boolean) {
        when (result) {
            is Resource.Success -> {
                val newBreeds = result.data.orEmpty()
                updateState {
                    it.copy(
                        breeds = mergeBreeds(it, newBreeds, isInitialLoad),
                        filteredBreeds = updateFilteredBreeds(it, newBreeds, isInitialLoad),
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        hasMoreData = newBreeds.isNotEmpty(),
                        currentPage = page,
                        lastUpdated = getCurrentDateTime(),
                        isStale = false
                    )
                }
            }
            is Resource.Error -> {
                val newBreeds = result.data.orEmpty()
                Log.d(
                    TAG,
                    "Error result: message=${result.message}, hasData=${newBreeds.isNotEmpty()}, hasShownError=${_uiState.value.hasShownError}, currentBreedsCount=${_uiState.value.breeds.size}, page=$page"
                )
                updateState {
                    val updatedBreeds = mergeBreeds(it, newBreeds, isInitialLoad)
                    val updatedFilteredBreeds = updateFilteredBreeds(it, newBreeds, isInitialLoad)
                    if (page == 1 && it.hasShownError) {
                        Log.d(TAG, "Ignoring repeat error for page 1: ${result.message}")
                        it.copy(
                            breeds = updatedBreeds,
                            filteredBreeds = updatedFilteredBreeds,
                            isLoading = false,
                            isLoadingMore = false
                        )
                    } else {
                        val errorMessage = if (page > 1) "Failed to load more data" else result.message
                        Log.d(TAG, "Setting new error: $errorMessage")
                        it.copy(
                            breeds = updatedBreeds,
                            filteredBreeds = updatedFilteredBreeds,
                            isLoading = false,
                            isLoadingMore = false,
                            error = errorMessage,
                            hasShownError = true
                        )
                    }
                }
            }
            is Resource.Loading -> {
                updateState {
                    it.copy(
                        isLoading = isInitialLoad,
                        isLoadingMore = !isInitialLoad,
                        error = null
                    )
                }
            }
        }
    }

    private fun toggleFavorite(breedId: String, isFavorite: Boolean) {
        updateBreedFavorite(breedId, isFavorite)
        viewModelScope.launch {
            updateFavoriteStatusUseCase(breedId, isFavorite)
                .catch { error ->
                    Log.e(TAG, "Toggle favorite error: ${error.message}")
                    revertFavorite(breedId, !isFavorite)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> Log.d(TAG, "Favorite updated for $breedId")
                        is Resource.Error -> {
                            Log.e(TAG, "Favorite update failed: ${result.message}")
                            revertFavorite(breedId, !isFavorite)
                        }
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    private fun updateSearchQuery(query: String) {
        Log.d(TAG, "updateSearchQuery: $query")
        updateState { it.copy(searchQuery = query) }
        searchDebouncer.updateQuery(query)
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            Log.d(TAG, "Performing search for query: $query")
            updateState { it.copy(isLoadingSearch = true) }
            searchBreedsUseCase(query)
                .catch { error ->
                    Log.e(TAG, "Search error: ${error.message}", error)
                    updateState {
                        it.copy(
                            isLoadingSearch = false,
                            filteredBreeds = emptyList(),
                            error = "Failed to search: ${error.message}",
                            hasShownError = true
                        )
                    }
                }
                .collect { result ->
                    Log.d(TAG, "Search result: $result")
                    updateState {
                        it.copy(
                            isLoadingSearch = false,
                            filteredBreeds = result.data.orEmpty(),
                            error = (result as? Resource.Error)?.message,
                            hasShownError = result is Resource.Error
                        )
                    }
                }
        }
    }

    private fun toggleFavoritesFilter() {
        updateState { state ->
            val showFavorites = !state.showingFavoritesOnly
            val filteredBreeds = if (showFavorites) state.breeds.filter { it.isFavorite } else state.breeds
            state.copy(
                showingFavoritesOnly = showFavorites,
                filteredBreeds = filteredBreeds
            )
        }
    }

    private fun refreshData() {
        updateState { it.copy(isStale = true, hasShownError = false) }
        loadInitialBreeds()
    }

    private fun toggleTheme() {
        // TODO: Implement theme toggling
    }

    private fun clearError() {
        updateState { it.copy(error = null) }
    }

    private fun clearSearchResults() {
        updateState { it.copy(filteredBreeds = it.breeds, isLoadingSearch = false) }
    }

    private fun shouldSkipLoad(isInitialLoad: Boolean): Boolean {
        val state = _uiState.value
        val shouldSkip = if (isInitialLoad) {
            state.isLoading || (state.breeds.isNotEmpty() && !state.isStale)
        } else {
            state.isLoadingMore
        }
        Log.d(
            TAG,
            "shouldSkipLoad: isInitialLoad=$isInitialLoad, shouldSkip=$shouldSkip, isLoading=${state.isLoading}, isLoadingMore=${state.isLoadingMore}, breedsCount=${state.breeds.size}, isStale=${state.isStale}"
        )
        return shouldSkip
    }

    private fun mergeBreeds(state: HomeScreenState, newBreeds: List<Cat>, isInitialLoad: Boolean): List<Cat> {
        return if (isInitialLoad) {
            newBreeds
        } else {
            val existingIds = state.breeds.map { it.id }.toSet()
            state.breeds + newBreeds.filter { it.id !in existingIds }
        }
    }

    private fun updateFilteredBreeds(state: HomeScreenState, newBreeds: List<Cat>, isInitialLoad: Boolean): List<Cat> {
        if (state.searchQuery.isNotBlank()) {
            // Preserve search results in filteredBreeds during loadBreeds
            return state.filteredBreeds
        }
        val updatedBreeds = mergeBreeds(state, newBreeds, isInitialLoad)
        return if (state.showingFavoritesOnly) updatedBreeds.filter { it.isFavorite } else updatedBreeds
    }

    private fun updateBreedFavorite(breedId: String, isFavorite: Boolean) {
        updateState { state ->
            val updateBreed: (Cat) -> Cat = { breed ->
                if (breed.id == breedId) breed.copy(isFavorite = isFavorite) else breed
            }
            state.copy(
                breeds = state.breeds.map(updateBreed),
                filteredBreeds = state.filteredBreeds.map(updateBreed)
            )
        }
    }

    private fun revertFavorite(breedId: String, originalFavorite: Boolean) {
        updateBreedFavorite(breedId, originalFavorite)
        updateState { it.copy(error = "Failed to update favorite status") }
    }

    private fun updateState(transform: (HomeScreenState) -> HomeScreenState) {
        _uiState.update(transform)
    }

    private fun getCurrentDateTime(): String {
        return LocalDateTime.now().toString() // Replace with your implementation
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val PAGE_SIZE = 10
    }
}
