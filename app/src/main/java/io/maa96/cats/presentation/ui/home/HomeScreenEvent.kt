package io.maa96.cats.presentation.ui.home

sealed class HomeScreenEvent {
    data class OnSearchQueryChange(val query: String) : HomeScreenEvent()
    data class ToggleFavorite(val breedId: String, val isFavorite: Boolean) : HomeScreenEvent()
    data object Refresh : HomeScreenEvent()
    data object ShowFavorites : HomeScreenEvent()
    data class ToggleTheme(val isDark: Boolean) : HomeScreenEvent()
    object LoadMoreBreeds : HomeScreenEvent()
    object ClearError : HomeScreenEvent()
}
