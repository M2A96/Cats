package io.maa96.cats.presentation.ui.details

import io.maa96.cats.domain.model.Cat

sealed class DetailScreenEvent {
    data class OnGetDetailResult(val breedId: String) : DetailScreenEvent()
    object Refresh : DetailScreenEvent()
    data class SelectImage(val index: Int) : DetailScreenEvent()
    data class ToggleFavorite(val breed: Cat) : DetailScreenEvent()
    object ClearError : DetailScreenEvent()
}
