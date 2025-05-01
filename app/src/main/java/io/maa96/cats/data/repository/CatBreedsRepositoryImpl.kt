package io.maa96.cats.data.repository

import androidx.room.withTransaction
import io.maa96.cats.data.dto.ExtractUrls
import io.maa96.cats.data.dto.toEntity
import io.maa96.cats.data.source.local.db.CatsDatabase
import io.maa96.cats.data.source.local.db.dao.BreedDao
import io.maa96.cats.data.source.local.db.entity.toDomain
import io.maa96.cats.data.source.remote.api.CatApi
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.model.toEntity
import io.maa96.cats.domain.repository.CatBreedsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class CatBreedsRepositoryImpl @Inject constructor(
    private val api: CatApi,
    private val dao: BreedDao,
    private val db: CatsDatabase
) : CatBreedsRepository() {
    override suspend fun getCatBreeds(limit: Int, page: Int): Flow<Resource<List<Cat>>> =
        networkBoundResource(
            query = {
                dao.getBreeds().map { entities ->
                    entities.map { it.toDomain() }
                }
            },
            fetch = {
                api.getCatBreeds(limit, page)
            },
            saveFetchedResult = {
                db.withTransaction {
                    dao.insertBreeds(it.map { it.toEntity() })
                }
            }
        )

    override suspend fun getCatBreedById(breedId: String): Flow<Resource<Cat>> = flow {
        try {
            emit(Resource.Loading())
            val breed = dao.getBreedById(breedId).toDomain()
            emit(Resource.Success(breed))
            emit(Resource.Loading(false))
        } catch (e: Exception) {
        }
    }

    override suspend fun searchBreeds(query: String, attachImage: Int) = networkBoundResource(
        query = {
            dao.searchByName(query).map { entities ->
                entities.map { it.toDomain() }
            }
        },
        fetch = {
            api.searchBreeds(query, attachImage)
        },
        saveFetchedResult = {
            db.withTransaction {
                dao.insertBreeds(it.map { it.toEntity() })
            }
        }
    )

    override suspend fun updateFavoriteStatus(breedId: String, isFav: Boolean): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            dao.updateFavStatus(breedId, isFav)
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred", false))
        }
    }

    override suspend fun getBreedImages(breedId: String): Flow<Resource<List<String>>> = networkBoundResource(
        query = {
            dao.getBreedImagesById(breedId).map { images ->
                images.firstOrNull()?.ExtractUrls() ?: listOf()
            }
        },
        fetch = {
            api.searchImages(breedIds = breedId)
        },
        saveFetchedResult = {
            db.withTransaction {
                dao.updateBreedImagesById(
                    images = it.map {
                        it.url
                    },
                    breedId = breedId
                )
            }
        }
    )
}
