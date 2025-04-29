package io.maa96.cats.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.usecase.GetCatBreedsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCatBreedsUseCase: GetCatBreedsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _catBreeds = MutableStateFlow<List<Cat>>(emptyList())

    @OptIn(FlowPreview::class)
    val filteredCats = combine(
        _catBreeds,
        _searchQuery.debounce(300)
    ) { breeds, query ->
        if (query.isBlank()) {
            breeds
        } else {
            breeds.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadCats()
    }

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
            }

            HomeScreenEvent.NavigateToFavorites -> navigateToFavorites()
            HomeScreenEvent.Refresh -> retry()
            is HomeScreenEvent.ToggleFavorite -> toggleFavorite(event.breedId)
            HomeScreenEvent.ToggleFilterDialog -> TODO()
            HomeScreenEvent.ToggleTheme -> TODO()
        }
    }

    private fun navigateToFavorites() {
        Log.d("TAG", "navigateToFavorites: Not Implemented Yet.")
    }

   private fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun toggleFavorite(id: String) {
        val updatedBreeds = _catBreeds.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        _catBreeds.value = updatedBreeds
    }

    private fun retry() {
        loadCats()
    }

    private fun loadCats() {
        viewModelScope.launch {
            getCatBreedsUseCase(limit = 10, page = 1)
                .catch {
                    Log.e("HomeViewModel", "getCatBreeds: error${it.message}")
                    _uiState.update { it.copy(isLoading = false) }
                }.collect { result ->
                    handleGetBreedsResult(result)
                }
        }
    }

    private fun handleGetBreedsResult(result: Resource<List<Cat>>) {
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
}