package io.maa96.cats.presentation.ui.home

import io.maa96.cats.domain.model.Cat

sealed class HomeScreenEvent {
    data class OnSearchQueryChange(val query: String) : HomeScreenEvent()
    data class ToggleFavorite(val breed: Cat) : HomeScreenEvent()
    data object Refresh : HomeScreenEvent()
    data object ShowFavorites : HomeScreenEvent()
    data object ToggleTheme : HomeScreenEvent()
    object LoadMoreBreeds : HomeScreenEvent()
    object ClearError : HomeScreenEvent()
}
