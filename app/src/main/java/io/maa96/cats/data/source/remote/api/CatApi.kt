package io.maa96.cats.data.source.remote.api

import io.maa96.cats.data.dto.CatBreed
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatApi {
    @GET("v1/breeds")
    suspend fun getCatBreeds(
        @Query("limit") limit: Int,
        @Query("page") page: Int,
    ): List<CatBreed>

    @GET("v1/breeds/{breed_id}")
    suspend fun getCatBreedById(
        @Path("breed_id") breedId: String,
    ): CatBreed

}