package io.maa96.cats.presentation.ui.details

import io.maa96.cats.domain.model.Cat

data class DetailScreenState(
    val catDetail: Cat? = null,
    val breedId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasShownError: Boolean = false,
    val selectedImageIndex: Int = 0,
    val lastUpdated: String = "",
    val isStale: Boolean = false
)
