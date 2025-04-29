package io.maa96.cats.data.mapper

import io.maa96.cats.data.dto.CatBreed
import io.maa96.cats.domain.model.Cat
import javax.inject.Inject

class CatBreedMapper @Inject constructor() {
    fun toDomain(catBreed: CatBreed): Cat {
        return Cat(
            id = catBreed.id,
            name = catBreed.name,
            images = catBreed.image?.let { listOf(it.url) },
            description = catBreed.description,
            temperament = catBreed.temperament,
            origin = catBreed.origin,
            lifeSpan = catBreed.lifeSpan,
            weight = catBreed.weight.imperial,
            hypoallergenic = catBreed.hypoallergenic,
            affectionLevel = catBreed.affectionLevel,
            childFriendly = catBreed.childFriendly,
            strangerFriendly = catBreed.strangerFriendly,
            wikipediaUrl = catBreed.wikipediaUrl,
            isFavorite = false,
        )
    }
}