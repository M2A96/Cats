package io.maa96.cats.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.maa96.cats.domain.model.Cat
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel :ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenState>(HomeScreenState())
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(id: String) {
        val updatedBreeds = _catBreeds.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        _catBreeds.value = updatedBreeds
    }

    fun retry() {
        loadCats()
    }

    private fun loadCats() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                // Simulate network delay
                delay(2000)

                // Mock data for demonstration
                val breeds = listOf(
                    Cat(
                        id = "beng",
                        name = "Bengal",
                        imageUrl = "https://cdn2.thecatapi.com/images/O3btzLlsO.png",
                        temperament = "Alert, Agile, Energetic, Demanding, Intelligent",
                        origin = "United States"
                    ),
                    Cat(
                        id = "siam",
                        name = "Siamese",
                        imageUrl = "https://cdn2.thecatapi.com/images/Kf57XGGxE.jpg",
                        temperament = "Active, Agile, Clever, Sociable, Loving, Energetic",
                        origin = "Thailand"
                    ),
                    Cat(
                        id = "mcoo",
                        name = "Maine Coon",
                        imageUrl = "https://cdn2.thecatapi.com/images/OOD3VXAQn.jpg",
                        temperament = "Adaptable, Intelligent, Loving, Gentle, Independent",
                        origin = "United States"
                    ),
                    Cat(
                        id = "ragd",
                        name = "Ragdoll",
                        imageUrl = "https://cdn2.thecatapi.com/images/oGefY4NWI.jpg",
                        temperament = "Affectionate, Friendly, Gentle, Quiet, Easygoing",
                        origin = "United States"
                    ),
                    Cat(
                        id = "esho",
                        name = "Exotic Shorthair",
                        imageUrl = "https://cdn2.thecatapi.com/images/KoK5Xq3xX.jpg",
                        temperament = "Affectionate, Sweet, Loyal, Quiet, Peaceful",
                        origin = "United States"
                    )
                )

                _catBreeds.value = breeds
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        breeds = breeds
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}