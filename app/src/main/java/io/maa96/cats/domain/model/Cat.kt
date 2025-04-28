package io.maa96.cats.domain.model

data class Cat(
    val id: String,
    val name: String,
    val imageUrl: String,
    val temperament: String,
    val origin: String,
    val isFavorite: Boolean = false
)
