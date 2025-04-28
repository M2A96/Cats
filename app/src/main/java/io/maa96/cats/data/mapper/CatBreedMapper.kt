package io.maa96.cats.data.mapper

import io.maa96.cats.data.dto.CatBreed
import io.maa96.cats.domain.model.Cat
import javax.inject.Inject

class CatBreedMapper @Inject constructor() {
    fun toDomain(catBreed: CatBreed): Cat {
        return Cat(
            id = catBreed.id,
            name = catBreed.name,
            imageUrl = catBreed.image.url,
            temperament = catBreed.temperament,
            isFavorite = false,
            origin = catBreed.origin
        )
    }
}