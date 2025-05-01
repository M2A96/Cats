package io.maa96.cats.presentation.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.usecase.GetBreedImagesUseCase
import io.maa96.cats.domain.usecase.GetCatBreedByIdUseCase
import io.maa96.cats.domain.usecase.UpdateFavoriteStatusUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCatBreedByIdUseCase: GetCatBreedByIdUseCase,
    private val getBreedImagesUseCase: GetBreedImagesUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>("breedId")?.let { breedId ->
            _uiState.update {
                it.copy(breedId = breedId)
            }
            onEvent(DetailScreenEvent.OnGetDetailResult(breedId))
        }
    }

    fun onEvent(event: DetailScreenEvent) {
        when (event) {
            is DetailScreenEvent.Refresh -> refresh()
            is DetailScreenEvent.SelectImage -> updateSelectedImage(event.index)
            is DetailScreenEvent.ToggleFavorite -> toggleFavorite(event.breed)
            is DetailScreenEvent.OnGetDetailResult -> {
                getCatBreedDetailById(event.breedId)
                getBreedImages(event.breedId)
            }
        }
    }

    private fun toggleFavorite(breed: Cat) {
        viewModelScope.launch {
            updateFavoriteStatusUseCase(breed.id, !breed.isFavorite)
                .catch {
                }
                .collect {
                    _uiState.update { currentState ->
                        currentState.copy(
                            catDetail = currentState.catDetail?.copy(isFavorite = !breed.isFavorite)
                        )
                    }
                }
        }
    }

    private fun updateSelectedImage(index: Int) {
        _uiState.update {
            it.copy(
                selectedImageIndex = index
            )
        }
    }

    private fun refresh() {
        val currentBreedId = _uiState.value.breedId
        getCatBreedDetailById(currentBreedId)
    }

    private fun getCatBreedDetailById(breedId: String) {
        viewModelScope.launch {
            getCatBreedByIdUseCase(breedId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message)
                    }
                }
                .collect { catBreed ->
                    updateUiState(catBreed)
                }
        }
    }

    private fun getBreedImages(breedId: String) {
        viewModelScope.launch {
            getBreedImagesUseCase(breedId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message)
                    }
                }
                .collect { catBreed ->
                    updateCatImages(catBreed)
                }
        }
    }

    private fun updateCatImages(catBreed: Resource<List<String>>) {
        when (catBreed) {
            is Resource.Error -> {
                _uiState.update {
                    it.copy(isLoading = false, error = catBreed.message)
                }
            }
            is Resource.Loading -> {
                _uiState.update {
                    it.copy(isLoading = true)
                }
            }
            is Resource.Success -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        catDetail = currentState.catDetail?.copy(images = catBreed.data)
                    )
                }
            }
        }
    }

    private fun updateUiState(catBreed: Resource<Cat>) {
        when (catBreed) {
            is Resource.Error -> {
                _uiState.update {
                    it.copy(isLoading = false, error = catBreed.message)
                }
            }
            is Resource.Loading -> {
                _uiState.update {
                    it.copy(isLoading = true)
                }
            }
            is Resource.Success -> {
                _uiState.update {
                    it.copy(isLoading = false, catDetail = catBreed.data)
                }
            }
        }
    }
}
