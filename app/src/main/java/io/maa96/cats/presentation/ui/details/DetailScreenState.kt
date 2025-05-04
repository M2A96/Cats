package io.maa96.cats.presentation.ui.details

import io.maa96.cats.domain.model.Cat

data class DetailScreenState(
    val breedId: String = "",
    val catDetail: Cat? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasShownError: Boolean = false,
    val selectedImageIndex: Int = 0,
    val isStale: Boolean = false,
    val lastUpdated: String = ""
)
