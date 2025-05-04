package io.maa96.cats.presentation.ui.home

import io.maa96.cats.domain.model.Cat

data class HomeScreenState(
    val searchQuery: String = "",
    val currentThemIsDark: Boolean = false,
    val breeds: List<Cat> = emptyList(),
    val filteredBreeds: List<Cat> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val isStale: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 0,
    val lastUpdated: String = "",
    val showingFavoritesOnly: Boolean = false,
    val hasShownError: Boolean = false
)
