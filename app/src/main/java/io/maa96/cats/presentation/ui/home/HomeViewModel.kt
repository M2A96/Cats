package io.maa96.cats.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.usecase.GetCatBreedsUseCase
import io.maa96.cats.domain.usecase.SearchBreedsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCatBreedsUseCase: GetCatBreedsUseCase,
    private val searchBreedsUseCase: SearchBreedsUseCase,
    private val searchDebouncer: SearchDebouncer
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        setupSearchDebouncing()
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
        searchBreedsUseCase(query, 0)
            .onStart { setLoading(true) }
            .catch { error ->
                Log.e("HomeViewModel", "getCatBreeds: error${error.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
            .collect { result ->
                updateUiState(result)
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
            is HomeScreenEvent.OnSearchQueryChange -> {}
            HomeScreenEvent.NavigateToFavorites -> navigateToFavorites()
            HomeScreenEvent.Refresh -> retry()
            is HomeScreenEvent.ToggleFavorite -> toggleFavorite(event.breedId)
            HomeScreenEvent.ToggleFilterDialog -> TODO()
            HomeScreenEvent.ToggleTheme -> TODO()
            HomeScreenEvent.LoadMoreBreeds -> TODO()
        }
    }

    private fun navigateToFavorites() {
        Log.d("TAG", "navigateToFavorites: Not Implemented Yet.v")
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchDebouncer.setQuery(query)
    }


    private fun toggleFavorite(id: String) {
        _uiState.update { currentState ->
            currentState.copy(
                breeds = currentState.breeds.map {
                    if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
                }
            )
        }
    }

    private fun retry() {
        loadBreeds()
    }

    private fun loadBreeds() {
        viewModelScope.launch {
            getCatBreedsUseCase(limit = 10, page = 1)
                .catch {
                    Log.e("HomeViewModel", "getCatBreeds: error${it.message}")
                    _uiState.update { it.copy(isLoading = false) }
                }.collect { result ->
                    updateUiState(result)
                }
        }
    }

    private fun updateUiState(result: Resource<List<Cat>>) {
        when (result) {
            is Resource.Error -> {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }

            is Resource.Loading -> {
                _uiState.update { it.copy(isLoading = true) }
            }

            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        breeds = result.data ?: emptyList()
                    )
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }
}