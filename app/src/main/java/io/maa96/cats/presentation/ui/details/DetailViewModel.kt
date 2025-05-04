package io.maa96.cats.presentation.ui.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.usecase.GetBreedImagesUseCase
import io.maa96.cats.domain.usecase.GetCatBreedByIdUseCase
import io.maa96.cats.domain.usecase.UpdateFavoriteStatusUseCase
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "DetailViewModel"

/**
 * ViewModel for the Detail screen that manages cat breed details, images, and favorite status.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCatBreedByIdUseCase: GetCatBreedByIdUseCase,
    private val getBreedImagesUseCase: GetBreedImagesUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailScreenState())
    val uiState: StateFlow<DetailScreenState> = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>("breedId")?.let { breedId ->
            updateState { it.copy(breedId = breedId) }
            onEvent(DetailScreenEvent.OnGetDetailResult(breedId))
        }
    }

    fun onEvent(event: DetailScreenEvent) {
        when (event) {
            is DetailScreenEvent.OnGetDetailResult -> fetchCatDetailsAndImages(event.breedId)
            is DetailScreenEvent.Refresh -> refresh()
            is DetailScreenEvent.SelectImage -> updateSelectedImage(event.index)
            is DetailScreenEvent.ToggleFavorite -> toggleFavorite(event.breed)
            is DetailScreenEvent.ClearError -> clearError()
        }.also { Log.d(TAG, "Processed event: $event") }
    }

    private fun fetchCatDetailsAndImages(breedId: String) {
        if (shouldSkipLoad()) {
            Log.d(TAG, "Skipping fetchCatDetailsAndImages: already loaded or loading")
            return
        }
        fetchCatDetails(breedId)
        fetchBreedImages(breedId)
    }

    private fun fetchCatDetails(breedId: String) {
        viewModelScope.launch {
            getCatBreedByIdUseCase(breedId)
                .onStart {
                    updateState { it.copy(isLoading = true) }
                }
                .catch { error ->
                    Log.e(TAG, "Fetch cat details error: ${error.message}", error)
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = error.message,
                            hasShownError = true
                        )
                    }
                }
                .collect { result ->
                    Log.d(TAG, "Collected cat details for breedId $breedId: $result")
                    handleCatBreedResult(result)
                }
        }
    }

    private fun fetchBreedImages(breedId: String) {
        viewModelScope.launch {
            getBreedImagesUseCase(breedId)
                .onStart {
                    updateState { it.copy(isLoading = true) }
                }
                .catch { error ->
                    Log.e(TAG, "Fetch breed images error: ${error.message}", error)
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = error.message,
                            hasShownError = true
                        )
                    }
                }
                .collect { result ->
                    Log.d(TAG, "Collected breed images for breedId $breedId: $result")
                    handleBreedImagesResult(result)
                }
        }
    }

    private fun handleCatBreedResult(result: Resource<Cat>) {
        when (result) {
            is Resource.Success -> {
                updateState {
                    it.copy(
                        catDetail = result.data,
                        isLoading = false,
                        error = null,
                        lastUpdated = getCurrentDateTime(),
                        isStale = false
                    )
                }
            }
            is Resource.Error -> {
                Log.d(
                    TAG,
                    "Error result: message=${result.message}, hasData=${result.data != null}, hasShownError=${_uiState.value.hasShownError}"
                )
                updateState {
                    if (it.hasShownError && result.data == null) {
                        Log.d(TAG, "Ignoring repeat error: ${result.message}")
                        it.copy(
                            catDetail = result.data ?: it.catDetail,
                            isLoading = false
                        )
                    } else {
                        it.copy(
                            catDetail = result.data ?: it.catDetail,
                            isLoading = false,
                            error = result.message,
                            hasShownError = true
                        )
                    }
                }
            }
            is Resource.Loading -> {
                updateState {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

    private fun handleBreedImagesResult(result: Resource<List<String>>) {
        when (result) {
            is Resource.Success -> {
                updateState { state ->
                    val updatedCatDetail = state.catDetail?.copy(images = result.data.orEmpty())
                    state.copy(
                        catDetail = updatedCatDetail,
                        isLoading = false,
                        error = null,
                        lastUpdated = getCurrentDateTime(),
                        isStale = false
                    )
                }
            }
            is Resource.Error -> {
                Log.d(
                    TAG,
                    "Error result: message=${result.message}, hasData=${!result.data.isNullOrEmpty()}, hasShownError=${_uiState.value.hasShownError}"
                )
                updateState {
                    val updatedCatDetail = if (!result.data.isNullOrEmpty()) {
                        it.catDetail?.copy(images = result.data)
                    } else {
                        it.catDetail
                    }
                    if (it.hasShownError && result.data.isNullOrEmpty()) {
                        Log.d(TAG, "Ignoring repeat error: ${result.message}")
                        it.copy(
                            catDetail = updatedCatDetail,
                            isLoading = false
                        )
                    } else {
                        it.copy(
                            catDetail = updatedCatDetail,
                            isLoading = false,
                            error = result.message,
                            hasShownError = true
                        )
                    }
                }
            }
            is Resource.Loading -> {
                updateState {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

    private fun toggleFavorite(breed: Cat) {
        val newFavoriteStatus = !breed.isFavorite
        updateBreedFavorite(breed.id, newFavoriteStatus)
        viewModelScope.launch {
            updateFavoriteStatusUseCase(breed.id, newFavoriteStatus)
                .catch { error ->
                    Log.e(TAG, "Toggle favorite error: ${error.message}", error)
                    revertFavorite(breed.id, breed.isFavorite)
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d(TAG, "Favorite updated for ${breed.id}")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Favorite update failed: ${result.message}")
                            revertFavorite(breed.id, breed.isFavorite)
                        }
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    private fun updateBreedFavorite(breedId: String, isFavorite: Boolean) {
        updateState { state ->
            val updatedCatDetail = state.catDetail?.let { cat ->
                if (cat.id == breedId) cat.copy(isFavorite = isFavorite) else cat
            }
            state.copy(catDetail = updatedCatDetail)
        }
    }

    private fun revertFavorite(breedId: String, originalFavorite: Boolean) {
        updateBreedFavorite(breedId, originalFavorite)
        updateState {
            it.copy(
                error = "Failed to update favorite status",
                hasShownError = true
            )
        }
    }

    private fun updateSelectedImage(index: Int) {
        updateState {
            it.copy(selectedImageIndex = index)
        }
    }

    private fun refresh() {
        updateState {
            it.copy(isStale = true, hasShownError = false)
        }
        val currentBreedId = _uiState.value.breedId
        if (currentBreedId.isNotBlank()) {
            fetchCatDetailsAndImages(currentBreedId)
        }
    }

    private fun clearError() {
        updateState { it.copy(error = null) }
    }

    private fun shouldSkipLoad(): Boolean {
        val state = _uiState.value
        val shouldSkip = state.isLoading || (state.catDetail != null && !state.isStale)
        Log.d(
            TAG,
            "shouldSkipLoad: shouldSkip=$shouldSkip, isLoading=${state.isLoading}, hasData=${state.catDetail != null}, isStale=${state.isStale}"
        )
        return shouldSkip
    }

    private fun updateState(transform: (DetailScreenState) -> DetailScreenState) {
        _uiState.update(transform)
    }

    private fun getCurrentDateTime(): String {
        return LocalDateTime.now().toString() // Replace with your implementation
    }

    companion object {
        private const val TAG = "DetailViewModel"
    }
}
