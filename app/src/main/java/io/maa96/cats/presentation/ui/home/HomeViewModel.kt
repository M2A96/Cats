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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
                    _uiState.update { it.copy(isLoading = false) }
                }.collect { result ->
                    updateUiState(result)
                }
        }
    }

    private fun updateUiState(result: Resource<List<Cat>>) {
        when (result) {
            is Resource.Error -> {
                // Check if we have data to show even with an error
                if (result.data.isNullOrEmpty().not()) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            breeds = result.data ?: listOf(),
                            isLoading = false,
                            error = result.message,
                            isStale = true
                        )
                    }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }

            is Resource.Loading -> {
                _uiState.update { it.copy(isLoading = true) }
            }

            is Resource.Success -> {
                _uiState.update {currentState ->
                    currentState.copy(
                        breeds = result.data ?: listOf(),
                        isLoading = false,
                        error = null,
                        isStale = false,
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