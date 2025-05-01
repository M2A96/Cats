package io.maa96.cats.data.source.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.maa96.cats.domain.model.Cat

@Entity(
    tableName = "breed"
)
data class CatBreedEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val images: List<String>?,
    val description: String,
    val temperament: String,
    val origin: String,
    val lifeSpan: String,
    val weight: String,
    val hypoallergenic: Int,
    val affectionLevel: Int,
    val childFriendly: Int,
    val strangerFriendly: Int,
    val wikipediaUrl: String?,
    val isFavorite: Boolean = false
)

fun CatBreedEntity.toDomain() = Cat(
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
    wikipediaUrl = wikipediaUrl ?: "",
    isFavorite = isFavorite
)
