package io.maa96.cats.data.dto

import android.util.Log
import io.maa96.cats.data.source.local.db.entity.CatBreedEntity

data class CatBreed(
    val weight: WeightDto,
    val id: String,
    val name: String,
    val cfaUrl: String,
    val vetstreetUrl: String,
    val vcahospitalsUrl: String,
    val temperament: String,
    val origin: String,
    val countryCodes: String,
    val countryCode: String,
    val description: String,
    val lifeSpan: String,
    val indoor: Int,
    val lap: Int,
    val altNames: String,
    val adaptability: Int,
    val affectionLevel: Int,
    val childFriendly: Int,
    val dogFriendly: Int,
    val energyLevel: Int,
    val grooming: Int,
    val healthIssues: Int,
    val intelligence: Int,
    val sheddingLevel: Int,
    val socialNeeds: Int,
    val strangerFriendly: Int,
    val vocalisation: Int,
    val experimental: Int,
    val hairless: Int,
    val natural: Int,
    val rare: Int,
    val rex: Int,
    val suppressedTail: Int,
    val shortLegs: Int,
    val wikipediaUrl: String,
    val hypoallergenic: Int,
    val referenceImageId: String,
    val image: ImageDto?
)

fun CatBreed.toEntity() = CatBreedEntity(
    id = id,
    index = id.hashCode(),
    name = name,
    images = image?.let { listOf(it.url) },
    description = description,
    temperament = temperament,
    origin = origin,
    lifeSpan = lifeSpan,
    weight = weight.toString(),
    hypoallergenic = hypoallergenic,
    affectionLevel = affectionLevel,
    childFriendly = childFriendly,
    strangerFriendly = strangerFriendly,
    wikipediaUrl = wikipediaUrl,
    isFavorite = false
)

fun String.ExtractUrls() = removeSurrounding("[", "]")
    ?.split(",")
    ?.map { it.trim().removeSurrounding("\"") }.also {
        Log.d("CatBreedsRepositoryImpl", "toStringList: $it")
    }
