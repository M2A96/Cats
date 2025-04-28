package io.maa96.cats.presentation.ui.details

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor() :ViewModel() {

    private val _uiState = MutableStateFlow(DetailScreenState())
    val uiState =  _uiState.asStateFlow()


    fun onEvent(event: DetailScreenEvent) {
        when (event) {
            DetailScreenEvent.NavigateBack -> TODO()
            DetailScreenEvent.OpenWikipedia -> TODO()
            DetailScreenEvent.Refresh -> TODO()
            is DetailScreenEvent.SelectImage -> TODO()
            DetailScreenEvent.ToggleFavorite -> TODO()
        }
    }
}