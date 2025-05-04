package io.maa96.cats.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.usecase.*
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCatBreedsUseCase: GetCatBreedsUseCase,
    private val searchBreedsUseCase: SearchBreedsUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    private val searchDebouncer: SearchDebouncer,
    private val saveThemeUseCase: SaveThemeUseCase,
    private val getThemeUseCase: GetThemeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    init {
        setupSearchDebouncing()
        getTheme()
    }

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnSearchQueryChange -> handleSearchQueryChange(event.query)
            HomeScreenEvent.ShowFavorites -> toggleFavoritesFilter()
            HomeScreenEvent.Refresh -> refreshData()
            is HomeScreenEvent.ToggleFavorite -> toggleFavorite(event.breedId, event.isFavorite)
            is HomeScreenEvent.ToggleTheme -> saveTheme(event.isDark)
            HomeScreenEvent.LoadMoreBreeds -> loadMoreBreeds()
            HomeScreenEvent.ClearError -> clearError()
        }.also { Log.d(TAG, "Processed event: $event") }
    }

    private fun setupSearchDebouncing() {
        viewModelScope.launch(Dispatchers.Main) {
            searchDebouncer.getQueryFlow().collect { query ->
                Log.d(TAG, "SearchDebouncer emitted query: $query")
                performDebouncedSearch(query)
            }
        }
    }

    private fun handleSearchQueryChange(query: String) {
        Log.d(TAG, "handleSearchQueryChange: $query")
        updateState {
            it.copy(searchQuery = query)
        }
        searchDebouncer.updateQuery(query)
    }

    private fun performDebouncedSearch(query: String) {
        if (query.isNotBlank()) {
            performSearch(query)
        } else {
            clearSearchResults()
            if (_uiState.value.breeds.isEmpty()) {
                loadInitialBreeds()
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            Log.d(TAG, "Performing search for query: $query")
            searchBreedsUseCase(query)
                .onStart {
                    updateState { it.copy(isLoading = true) }
                }
                .catch { error ->
                    Log.e(TAG, "Search error: ${error.message}", error)
                    updateState {
                        it.copy(
                            isLoading = false,
                            filteredBreeds = emptyList(),
                            error = "Failed to search: ${error.message}",
                            hasShownError = true
                        )
                    }
                }
                .collect { result ->
                    Log.d(TAG, "Search result for '$query': $result")
                    handleSearchResult(result)
                }
        }
    }

    private fun handleSearchResult(result: Resource<List<Cat>>) {
        when (result) {
            is Resource.Success -> {
                updateState {
                    it.copy(
                        isLoading = false,
                        filteredBreeds = result.data.orEmpty(),
                        error = null
                    )
                }
            }
            is Resource.Error -> {
                updateState {
                    it.copy(
                        isLoading = false,
                        filteredBreeds = result.data.orEmpty(),
                        error = result.message,
                        hasShownError = true
                    )
                }
            }
            is Resource.Loading -> {
                updateState {
                    it.copy(isLoading = true)
                }
            }
        }
    }

    private fun clearSearchResults() {
        updateState { state ->
            // When clearing search, show either favorites or all breeds
            val displayedBreeds = if (state.showingFavoritesOnly) {
                state.breeds.filter { it.isFavorite }
            } else {
                state.breeds
            }
            state.copy(
                filteredBreeds = displayedBreeds,
                isLoading = false
            )
        }
    }

    private fun loadInitialBreeds() {
        loadBreeds(page = 0, isInitialLoad = true)
    }
    private fun getTheme() {
        viewModelScope.launch(Dispatchers.IO) {
            getThemeUseCase()
                .catch {
                    updateState { it.copy(currentThemIsDark = true) }
                }
                .collect { isThemeDark ->
                    updateState { it.copy(currentThemIsDark = isThemeDark) }
                }
        }
    }

    private fun saveTheme(themeIsDark: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            saveThemeUseCase(themeIsDark.not())
            updateState { it.copy(currentThemIsDark = themeIsDark.not()) }
        }
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
                .onStart {
                    updateState {
                        it.copy(
                            isLoading = isInitialLoad,
                            isLoadingMore = !isInitialLoad
                        )
                    }
                }
                .catch { error ->
                    Log.e(TAG, "Load breeds error: ${error.message}", error)
                    updateState {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = error.message,
                            hasShownError = true
                        )
                    }
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
                updateState { state ->
                    val updatedBreeds = mergeBreeds(state, newBreeds, isInitialLoad)
                    val updatedFilteredBreeds = if (state.searchQuery.isNotBlank()) {
                        state.filteredBreeds
                    } else if (state.showingFavoritesOnly) {
                        updatedBreeds.filter { it.isFavorite }
                    } else {
                        updatedBreeds
                    }

                    state.copy(
                        breeds = updatedBreeds,
                        filteredBreeds = updatedFilteredBreeds,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        hasMoreData = newBreeds.size >= PAGE_SIZE,
                        currentPage = page,
                        lastUpdated = getCurrentDateTime(),
                        isStale = false
                    )
                }
            }
            is Resource.Error -> {
                val newBreeds = result.data.orEmpty()
                handleErrorResult(result, page, newBreeds, isInitialLoad)
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

    private fun handleErrorResult(result: Resource.Error<List<Cat>>, page: Int, newBreeds: List<Cat>, isInitialLoad: Boolean) {
        Log.d(
            TAG,
            "Error result: message=${result.message}, hasData=${newBreeds.isNotEmpty()}, hasShownError=${_uiState.value.hasShownError}, page=$page"
        )

        updateState { state ->
            val updatedBreeds = mergeBreeds(state, newBreeds, isInitialLoad)
            val updatedFilteredBreeds = if (state.searchQuery.isNotBlank()) {
                state.filteredBreeds
            } else if (state.showingFavoritesOnly) {
                updatedBreeds.filter { it.isFavorite }
            } else {
                updatedBreeds
            }

            if (page == 0 && state.hasShownError) {
                Log.d(TAG, "Ignoring repeat error for page 0: ${result.message}")
                state.copy(
                    breeds = updatedBreeds,
                    filteredBreeds = updatedFilteredBreeds,
                    isLoading = false,
                    isLoadingMore = false
                )
            } else {
                val errorMessage = if (page > 0) "Failed to load more data" else result.message
                Log.d(TAG, "Setting new error: $errorMessage")
                state.copy(
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

    private fun toggleFavoritesFilter() {
        updateState { state ->
            val showFavorites = !state.showingFavoritesOnly
            val filteredBreeds = if (showFavorites) {
                state.breeds.filter { it.isFavorite }
            } else {
                state.breeds
            }
            state.copy(
                showingFavoritesOnly = showFavorites,
                filteredBreeds = filteredBreeds,
                searchQuery = "" // Clear search when toggling favorites
            )
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

    private fun updateBreedFavorite(breedId: String, isFavorite: Boolean) {
        updateState { state ->
            val updateBreed: (Cat) -> Cat = { breed ->
                if (breed.id == breedId) breed.copy(isFavorite = isFavorite) else breed
            }
            val updatedBreeds = state.breeds.map(updateBreed)
            val updatedFilteredBreeds = state.filteredBreeds.map(updateBreed)

            state.copy(
                breeds = updatedBreeds,
                filteredBreeds = if (state.showingFavoritesOnly && !isFavorite) {
                    // Remove from filtered list if unfavorited while showing favorites
                    updatedFilteredBreeds.filter { it.id != breedId }
                } else {
                    updatedFilteredBreeds
                }
            )
        }
    }

    private fun revertFavorite(breedId: String, originalFavorite: Boolean) {
        updateBreedFavorite(breedId, originalFavorite)
        updateState { it.copy(error = "Failed to update favorite status") }
    }

    private fun refreshData() {
        updateState { it.copy(isStale = true, hasShownError = false) }
        loadInitialBreeds()
    }

    private fun clearError() {
        updateState { it.copy(error = null) }
    }

    private fun shouldSkipLoad(isInitialLoad: Boolean): Boolean {
        val state = _uiState.value
        val shouldSkip = if (isInitialLoad) {
            state.isLoading || (state.breeds.isNotEmpty() && !state.isStale)
        } else {
            state.isLoadingMore || !state.hasMoreData
        }
        Log.d(
            TAG,
            "shouldSkipLoad: isInitialLoad=$isInitialLoad, shouldSkip=$shouldSkip, isLoading=${state.isLoading}, isLoadingMore=${state.isLoadingMore}, breedsCount=${state.breeds.size}, isStale=${state.isStale}"
        )
        return shouldSkip
    }

    private fun mergeBreeds(state: HomeScreenState, newBreeds: List<Cat>, isInitialLoad: Boolean): List<Cat> = if (isInitialLoad) {
        newBreeds
    } else {
        val existingIds = state.breeds.map { it.id }.toSet()
        state.breeds + newBreeds.filter { it.id !in existingIds }
    }

    private fun updateState(transform: (HomeScreenState) -> HomeScreenState) {
        _uiState.update(transform)
    }

    private fun getCurrentDateTime(): String = LocalDateTime.now().toString()

    companion object {
        private const val TAG = "HomeViewModel"
        private const val PAGE_SIZE = 10
    }
}
