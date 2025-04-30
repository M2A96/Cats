package io.maa96.cats.presentation.ui.home

sealed class HomeScreenEvent {
    data class OnSearchQueryChange(val query: String) : HomeScreenEvent()
    data class ToggleFavorite(val breedId: String) : HomeScreenEvent()
    data object Refresh : HomeScreenEvent()
    data object NavigateToFavorites : HomeScreenEvent()
    data object ToggleFilterDialog : HomeScreenEvent()
    data object ToggleTheme : HomeScreenEvent()
    object LoadMoreBreeds : HomeScreenEvent()
    object ClearError : HomeScreenEvent()
}