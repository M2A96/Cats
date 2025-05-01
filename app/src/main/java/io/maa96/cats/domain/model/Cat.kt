package io.maa96.cats.domain.model

import io.maa96.cats.data.source.local.db.entity.CatBreedEntity

data class Cat(
    val id: String,
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

fun Cat.toEntity() = CatBreedEntity(
    id = id,
    name = name,
    images = images,
    description = description,
    origin = origin,
    temperament = temperament,
    lifeSpan = lifeSpan,
    weight = weight,
    hypoallergenic = hypoallergenic,
    affectionLevel = affectionLevel,
    childFriendly = childFriendly,
    strangerFriendly = strangerFriendly,
    wikipediaUrl = wikipediaUrl,
    isFavorite = isFavorite
)
