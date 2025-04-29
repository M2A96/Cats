package io.maa96.cats.presentation.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.usecase.GetCatBreedByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCatBreedByIdUseCase: GetCatBreedByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>("breedId")?.let { breedId ->
            onEvent(DetailScreenEvent.OnGetDetailResult(breedId))
        }
    }

    fun onEvent(event: DetailScreenEvent) {
        when (event) {
            DetailScreenEvent.NavigateBack -> TODO()
            is DetailScreenEvent.Refresh -> refresh()
            is DetailScreenEvent.SelectImage -> TODO()
            DetailScreenEvent.ToggleFavorite -> TODO()
            is DetailScreenEvent.OnGetDetailResult -> getCatBreedDetailById(event.breedId)
            is DetailScreenEvent.OpenWikipedia -> openWikipediaPage(event.wikipediaUrl)
        }
    }

    private fun refresh(){
        val currentBreedId = _uiState.value.breedId
        getCatBreedDetailById(currentBreedId)
    }

    private fun openWikipediaPage(url: String){

    }

    private fun getCatBreedDetailById(breedId: String) {
        viewModelScope.launch {
            getCatBreedByIdUseCase(breedId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error =throwable.message)
                    }
                }
                .collect { catBreed ->
                    updateUiState(catBreed)
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