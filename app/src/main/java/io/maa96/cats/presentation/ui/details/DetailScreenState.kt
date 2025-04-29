package io.maa96.cats.presentation.ui.details

import io.maa96.cats.domain.model.Cat

data class DetailScreenState(
    val isLoading: Boolean = false,
    val catDetail: Cat? = null,
    val error: String? = null,
    val selectedImageIndex: Int = 0
)