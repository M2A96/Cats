package io.maa96.cats.presentation.ui.webview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class WebViewViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _uiState = MutableStateFlow(WebViewState())
    val uiState = _uiState.asStateFlow()

    init {
        // Get URL and title from SavedStateHandle
        val url = savedStateHandle.get<String>("url") ?: ""
        val title = savedStateHandle.get<String>("title") ?: "Wikipedia"

        _uiState.update { currentState ->
            currentState.copy(
                url = url,
                pageTitle = title
            )
        }
    }

    fun onEvent(event: WebViewEvent) {
        when (event) {
            is WebViewEvent.OnPageStartLoading -> {
                _uiState.update { it.copy(isLoading = true) }
            }
            is WebViewEvent.OnPageFinishedLoading -> {
                _uiState.update { it.copy(isLoading = false) }
            }
            is WebViewEvent.OnPageLoadError -> {
                _uiState.update { it.copy(isLoading = false, hasError = true) }
            }
            is WebViewEvent.ClearError -> {
                _uiState.update { it.copy(hasError = false) }
            }
            is WebViewEvent.UrlLoaded -> {
                _uiState.update { it.copy(urlLoadedOnce = true) }
            }
        }
    }
}
