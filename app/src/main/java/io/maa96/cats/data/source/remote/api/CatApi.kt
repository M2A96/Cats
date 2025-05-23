package io.maa96.cats.data.source.remote.api

import io.maa96.cats.data.dto.CatBreed
import io.maa96.cats.data.dto.CatBreedImageDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatApi {
    @GET("v1/breeds")
    suspend fun getCatBreeds(
        @Query("limit") limit: Int,
        @Query("page") page: Int
    ): List<CatBreed>

    @GET("v1/breeds/{breed_id}")
    suspend fun getCatBreedById(
        @Path("breed_id") breedId: String
    ): CatBreed

    @GET("v1/breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String,
        @Query("attach_image") attachImage: Int
    ): List<CatBreed>

    @GET("v1/images/search")
    suspend fun searchImages(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 0,
        @Query("order") order: String = "RANDOM",
        @Query("breed_ids") breedIds: String
    ): List<CatBreedImageDto>
}
