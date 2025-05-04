package io.maa96.cats.domain.model

data class Cat(
    val id: String,
    val index: Int,
    val name: String,
    val images: List<String>?,
    val description: String,
    val origin: String,
    val temperament: String,
    val lifeSpan: String,
    val weight: String,
    val hypoallergenic: Int,
    val affectionLevel: Int, // 1-5
    val childFriendly: Int, // 1-5
    val strangerFriendly: Int, // 1-5
    val wikipediaUrl: String,
    val isFavorite: Boolean
)
