package io.maa96.cats.presentation.ui.webview

data class WebViewState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val url: String = "",
    val pageTitle: String = "Wikipedia",
    val urlLoadedOnce: Boolean = false
)
