package io.maa96.cats.presentation.ui.home

import io.maa96.cats.domain.model.Cat

data class HomeScreenState(
    val searchQuery: String = "",
    val breeds: List<Cat> = emptyList(),
    val filteredBreeds: List<Cat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showFilterDialog: Boolean = false
)
