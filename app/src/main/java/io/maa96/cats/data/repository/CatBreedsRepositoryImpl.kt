package io.maa96.cats.data.repository

import io.maa96.cats.data.mapper.CatBreedMapper
import io.maa96.cats.data.source.remote.api.CatApi
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.repository.CatBreedsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
            emit(Resource.Loading(false))
            emit(Resource.Success(catBreeds))
        } catch (e: Exception) {
            emit(Resource.Loading(false))
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getCatBreedById(breedId: String): Flow<Resource<Cat>> = flow {
        try {
            emit(Resource.Loading(true))
            val response = api.getCatBreedById(breedId)
            val catBreed = catBreedMapper.toDomain(response)
            emit(Resource.Loading(false))
            emit(Resource.Success(catBreed))
        } catch (e: Exception) {
            emit(Resource.Loading(false))
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * This is a generic function for making a network request and caching it into database
     * [ResultType] is type of result that we get from our network request
     * [RequestType] is requesting type of our network request
     * @param query is our database query that we make to get data from database
     * @param fetch is a higher order function that responsible for making our network request
     * @param saveFetchedResult is a higher order function that saves our network response to our database
     * @param shouldFetch is a higher order function that decides whether our response should save in database our not
     * */
    private inline fun <ResultType, RequestType> networkBoundResource(
        crossinline query: () -> Flow<ResultType>,
        crossinline fetch: suspend () -> RequestType,
        crossinline saveFetchedResult: suspend (RequestType) -> Unit,
        crossinline shouldFetch: (ResultType) -> Boolean = { true }
    ) = flow {
        val data = query().first()

        val flow = if (shouldFetch(data)) {
            emit(Resource.Loading(true))

            try {
                saveFetchedResult(fetch())
                query().map { Resource.Success(it) }
            } catch (throwable: Throwable) {
                query().map { Resource.Error(throwable.message!!,it) }
            }
        } else {
            query().map { Resource.Success(it) }
        }
        emitAll(flow)
    }
}