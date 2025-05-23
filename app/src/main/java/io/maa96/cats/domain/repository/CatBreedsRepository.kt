package io.maa96.cats.domain.repository

import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import kotlinx.coroutines.flow.Flow

abstract class CatBreedsRepository : BaseRepository() {
    abstract suspend fun getCatBreeds(limit: Int, page: Int): Flow<Resource<List<Cat>>>
    abstract suspend fun getCatBreedById(breedId: String): Flow<Resource<Cat>>
    abstract suspend fun searchBreeds(query: String, attachImage: Int): Flow<Resource<List<Cat>>>
    abstract suspend fun updateFavoriteStatus(breedId: String, isFav: Boolean): Flow<Resource<Boolean>>
    abstract suspend fun getBreedImages(breedId: String): Flow<Resource<List<String>>>
}
