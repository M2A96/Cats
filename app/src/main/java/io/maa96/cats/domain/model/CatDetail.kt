package io.maa96.cats.domain.model

data class CatDetail(
    val id: String,
    val name: String,
    val images: List<String>,
    val description: String,
    val temperament: String,
    val origin: String,
    val lifespan: String,
    val weight: String,
    val isHypoallergenic: Boolean,
    val affectionLevel: Int, // 1-5
    val childFriendly: Int, // 1-5
    val strangerFriendly: Int, // 1-5
    val wikipediaUrl: String,
    val isFavorite: Boolean = false
)