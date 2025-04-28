package io.maa96.cats.domain.repository

import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface CatBreedsRepository {
    suspend fun getCatBreeds(limit: Int, page: Int) : Flow<Resource<List<Cat>>>
}