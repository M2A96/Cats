package io.maa96.cats.presentation.ui.details

sealed class DetailScreenEvent {
    data class OnGetDetailResult(val breedId: String) : DetailScreenEvent()
    object Refresh : DetailScreenEvent()
    object ToggleFavorite : DetailScreenEvent()
    data class SelectImage(val index: Int) : DetailScreenEvent()
}
