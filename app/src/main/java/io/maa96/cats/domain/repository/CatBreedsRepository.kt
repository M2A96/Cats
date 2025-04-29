package io.maa96.cats.domain.repository

import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface CatBreedsRepository {
    suspend fun getCatBreeds(limit: Int, page: Int) : Flow<Resource<List<Cat>>>
    suspend fun getCatBreedById(breedId: String) : Flow<Resource<Cat>>
    suspend fun searchBreeds(query: String, attachImage: Int) : Flow<Resource<List<Cat>>>
}