package io.maa96.cats.data.repository

import io.maa96.cats.data.dto.CatBreed
import io.maa96.cats.data.mapper.CatBreedMapper
import io.maa96.cats.data.source.remote.api.CatApi
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.repository.CatBreedsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CatBreedsRepositoryImpl @Inject constructor(
    private val api: CatApi,
    private val catBreedMapper: CatBreedMapper
) : CatBreedsRepository {
    override suspend fun getCatBreeds(limit: Int, page: Int): Flow<Resource<List<Cat>>> = flow {
        try {
            emit(Resource.Loading(true))
            val response = api.getCatBreeds(
                limit = limit, page = page
            )
            val catBreeds = response.map { catBreed ->
                catBreedMapper.toDomain(catBreed)
            }
            emit(Resource.Success(catBreeds))
            emit(Resource.Loading(false))
        } catch (e: Exception) {
            emit(Resource.Loading(false))
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}