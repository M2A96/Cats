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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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

    private suspend fun performSearch(query: String) {
        // Reset pagination when performing a new search
        _uiState.update { it.copy(currentPage = 0, hasMoreData = true) }
        
        searchBreedsUseCase(query, 0)
            .onStart { setLoading(true) }
            .catch { error ->
                Log.e("HomeViewModel", "getCatBreeds: error${error.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
            .collect { result ->
                updateUiState(result, isInitialLoad = true)
            }
    }

    private fun clearSearchResults() {
        _uiState.update {
            it.copy(
                breeds = emptyList(),
                isLoading = false,
            )
        }
    }

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnSearchQueryChange -> updateSearchQuery(event.query)
            HomeScreenEvent.NavigateToFavorites -> navigateToFavorites()
            HomeScreenEvent.Refresh -> retry()
            is HomeScreenEvent.ToggleFavorite -> toggleFavorite(event.breed)
            HomeScreenEvent.ToggleFilterDialog -> TODO()
            HomeScreenEvent.ToggleTheme -> TODO()
            HomeScreenEvent.LoadMoreBreeds -> loadMoreBreeds()
            HomeScreenEvent.ClearError -> clearError()
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    private fun navigateToFavorites() {
        Log.d("TAG", "navigateToFavorites: Not Implemented Yet.v")
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchDebouncer.setQuery(query)
    }



    private fun toggleFavorite(breed: Cat) {
        // Store the original favorite status to revert if needed
        val originalIsFavorite = breed.isFavorite
        val updatedBreed = breed.copy(isFavorite = !originalIsFavorite)
        
        // Update UI state with the new favorite status (optimistic update)
        // Preserve the original order by using indexed map to update only the target item
        _uiState.update { currentState ->
            // Create a new list with the same order, just updating the target item
            val updatedList = currentState.breeds.toMutableList()
            val index = updatedList.indexOfFirst { it.id == breed.id }
            if (index != -1) {
                updatedList[index] = updatedBreed
            }
            
            currentState.copy(breeds = updatedList)
        }
        
        viewModelScope.launch {
            updateFavoriteStatusUseCase(updatedBreed)
                .catch { error ->
                    Log.e("HomeViewModel", "toggleFavorite error: ${error.message}")
                    // Revert UI state if operation fails
                    revertFavoriteStatus(breed.id, originalIsFavorite)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Error -> {
                            Log.e("HomeViewModel", "toggleFavorite failed: ${result.message}")
                            // Revert UI state if database update fails
                            revertFavoriteStatus(breed.id, originalIsFavorite)
                        }
                        is Resource.Success -> {
                            Log.d("HomeViewModel", "toggleFavorite success for breed ${breed.id}")
                            // Success case - UI is already updated
                        }
                        is Resource.Loading -> {
                            // Loading state is handled by optimistic UI update
                        }
                    }
                }
        }
    }
    
    private fun revertFavoriteStatus(breedId: String, originalIsFavorite: Boolean) {
        _uiState.update { currentState ->
            // Create a new list with the same order, just updating the target item
            val updatedList = currentState.breeds.toMutableList()
            val index = updatedList.indexOfFirst { it.id == breedId }
            if (index != -1) {
                updatedList[index] = updatedList[index].copy(isFavorite = originalIsFavorite)
            }
            
            currentState.copy(breeds = updatedList)
        }
    }

    private fun retry() {
        _uiState.update { it.copy(currentPage = 0) }
        loadBreeds()
    }

    private fun loadBreeds() {
        viewModelScope.launch {
            // Always start with page 1 for initial load
            val page = 1
            
            getCatBreedsUseCase(limit = 10, page = page)
                .onStart { setLoading(true) }
                .catch {
                    _uiState.update { it.copy(isLoading = false) }
                }.collect { result ->
                    updateUiState(result, isInitialLoad = true)
                    _uiState.update { it.copy(currentPage = page) }
                }
        }
    }
    
    private fun loadMoreBreeds() {
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore || !_uiState.value.hasMoreData) {
            return
        }
        
        viewModelScope.launch {
            val currentState = _uiState.value
            val nextPage = currentState.currentPage + 1
            
            _uiState.update { it.copy(isLoadingMore = true) }
            
            getCatBreedsUseCase(limit = 10, page = nextPage)
                .catch { error ->
                    Log.e("HomeViewModel", "loadMoreBreeds error: ${error.message}")
                    _uiState.update { it.copy(isLoadingMore = false) }
                }.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val newBreeds = result.data ?: emptyList()
                            if (newBreeds.isEmpty()) {
                                _uiState.update { it.copy(
                                    isLoadingMore = false,
                                    hasMoreData = false
                                )}
                            } else {
                                val updatedBreeds = currentState.breeds + newBreeds
                                _uiState.update { it.copy(
                                    breeds = updatedBreeds,
                                    isLoadingMore = false,
                                    currentPage = nextPage,
                                    error = null,
                                    isStale = false,
                                    lastUpdated = getCurrentDateTime()
                                )}
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(
                                isLoadingMore = false,
                                error = result.message
                            )}
                        }
                        is Resource.Loading -> {
                            // Already handled by setting isLoadingMore = true
                        }
                    }
                }
        }
    }

    private fun updateUiState(result: Resource<List<Cat>>, isInitialLoad: Boolean = false) {
        when (result) {
            is Resource.Error -> {
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

            is Resource.Loading -> {
                if (isInitialLoad) {
                    _uiState.update { it.copy(isLoading = true) }
                } else {
                    _uiState.update { it.copy(isLoadingMore = true) }
                }
            }

            is Resource.Success -> {
                val newData = result.data ?: listOf()
                _uiState.update { currentState ->
                    // If we're loading initial data and we already have breeds with favorite status
                    // we need to preserve the favorite status of existing items
                    val processedData = if (isInitialLoad && currentState.breeds.isNotEmpty()) {
                        // Create a map of existing breeds by ID for quick lookup
                        val existingBreedsMap = currentState.breeds.associateBy { it.id }
                        
                        // For each new breed, preserve its favorite status if it exists in our current list
                        newData.map { newBreed ->
                            existingBreedsMap[newBreed.id]?.let { existingBreed ->
                                // Keep the favorite status from the existing breed
                                newBreed.copy(isFavorite = existingBreed.isFavorite)
                            } ?: newBreed // Use new breed as is if not in our current list
                        }
                    } else if (!isInitialLoad && newData.isNotEmpty()) {
                        // For pagination (loading more), we need to preserve the order and favorite status
                        val existingBreedsMap = currentState.breeds.associateBy { it.id }
                        val newBreedsList = currentState.breeds.toMutableList()
                        
                        // Add only new breeds that aren't already in the list
                        newBreedsList.addAll(newData.filter { newBreed -> !existingBreedsMap.containsKey(newBreed.id) })
                        newBreedsList
                    } else {
                        // Just use the new data as is
                        newData
                    }
                    
                    currentState.copy(
                        breeds = processedData,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        isStale = false,
                        hasMoreData = newData.isNotEmpty(),
                        lastUpdated = getCurrentDateTime()
                    )
                }
            }
        }
    }

    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }
    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }
}