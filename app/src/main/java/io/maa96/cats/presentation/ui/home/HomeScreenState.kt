package io.maa96.cats.presentation.ui.home

import io.maa96.cats.domain.model.Cat

data class HomeScreenState(
    val breeds: List<Cat> = emptyList(),
    val filteredBreeds: List<Cat> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoadingSearch: Boolean = false,
    val error: String? = null,
    val isStale: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 1,
    val lastUpdated: String = "",
    val searchQuery: String = "",
    val showingFavoritesOnly: Boolean = false,
    val hasShownError: Boolean = false
)
