package io.maa96.cats.presentation.ui.webview

sealed class WebViewEvent {
    data object OnPageStartLoading : WebViewEvent()
    data object OnPageFinishedLoading : WebViewEvent()
    data object OnPageLoadError : WebViewEvent()
    data object ClearError : WebViewEvent()
    data object UrlLoaded : WebViewEvent()
}
