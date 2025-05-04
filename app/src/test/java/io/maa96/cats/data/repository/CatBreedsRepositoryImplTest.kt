package io.maa96.cats.data.repository

import androidx.room.withTransaction
import io.maa96.cats.data.source.local.db.CatsDatabase
import io.maa96.cats.data.source.local.db.dao.BreedDao
import io.maa96.cats.data.source.local.db.entity.CatBreedEntity
import io.maa96.cats.data.source.local.db.entity.toDomain
import io.maa96.cats.data.source.remote.api.CatApi
import io.maa96.cats.domain.model.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CatBreedsRepositoryImplTest {
    private lateinit var repository: CatBreedsRepositoryImpl
    private val api: CatApi = mockk()
    private val dao: BreedDao = mockk()
    private val db: CatsDatabase = mockk()

    @Before
    fun setup() {
        repository = CatBreedsRepositoryImpl(api, dao, db)
    }

    @Test
    fun `getCatBreeds should return breeds from local database`() = runTest {
        // Given
        val mockBreeds = listOf(
            CatBreedEntity(
                id = "1",
                index = 1,
                name = "Siamese",
                isFavorite = false,
                images = listOf("siamese1.jpg", "siamese2.jpg"),
                description = "The Siamese cat is one of the first distinctly recognized breeds of Asian cat.",
                temperament = "Active, Intelligent, Social",
                origin = "Thailand",
                lifeSpan = "12 - 15 years",
                weight = "8 - 15 pounds",
                hypoallergenic = 1,
                affectionLevel = 5,
                childFriendly = 4,
                strangerFriendly = 3,
                wikipediaUrl = "https://en.wikipedia.org/wiki/Siamese_cat"
            ),
            CatBreedEntity(
                id = "2",
                index = 2,
                name = "Persian",
                isFavorite = true,
                images = listOf("persian1.jpg", "persian2.jpg"),
                description = "The Persian cat is a long-haired breed of cat characterized by its round face.",
                temperament = "Gentle, Quiet, Affectionate",
                origin = "Iran (Persia)",
                lifeSpan = "10 - 15 years",
                weight = "7 - 12 pounds",
                hypoallergenic = 0,
                affectionLevel = 4,
                childFriendly = 3,
                strangerFriendly = 2,
                wikipediaUrl = "https://en.wikipedia.org/wiki/Persian_cat"
            )
        )
        coEvery { dao.getBreeds() } returns flowOf(mockBreeds)
        coEvery { api.getCatBreeds(any(), any()) } returns emptyList()
        coEvery { db.withTransaction(any<suspend () -> Any>()) } coAnswers { firstArg<suspend () -> Any>().invoke() }
        coEvery { dao.insertBreeds(any()) } returns Unit

        // When
        val result = repository.getCatBreeds(10, 1).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(mockBreeds.map { it.toDomain() }, (result as Resource.Success).data)
    }

    @Test
    fun `updateFavoriteStatus should update breed favorite status in database`() = runTest {
        // Given
        val breedId = "1"
        val isFavorite = true
        coEvery { dao.updateFavStatus(breedId, isFavorite) } returns Unit

        // When
        val result = repository.updateFavoriteStatus(breedId, isFavorite).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(true, (result as Resource.Success).data)
        coVerify { dao.updateFavStatus(breedId, isFavorite) }
    }

    @Test
    fun `searchBreeds should return matching breeds from database`() = runTest {
        // Given
        val query = "Siamese"
        val mockBreeds = listOf(
            CatBreedEntity(
                id = "1",
                index = 1,
                name = "Siamese",
                isFavorite = false,
                images = listOf("siamese1.jpg", "siamese2.jpg"),
                description = "The Siamese cat is one of the first distinctly recognized breeds of Asian cat.",
                temperament = "Active, Intelligent, Social",
                origin = "Thailand",
                lifeSpan = "12 - 15 years",
                weight = "8 - 15 pounds",
                hypoallergenic = 1,
                affectionLevel = 5,
                childFriendly = 4,
                strangerFriendly = 3,
                wikipediaUrl = "https://en.wikipedia.org/wiki/Siamese_cat"
            )
        )
        coEvery { dao.searchByName(query) } returns flowOf(mockBreeds)
        coEvery { api.searchBreeds(query, any()) } returns emptyList()
        coEvery { db.withTransaction(any<suspend () -> Any>()) } coAnswers { firstArg<suspend () -> Any>().invoke() }
        coEvery { dao.insertBreeds(any()) } returns Unit

        // When
        val result = repository.searchBreeds(query, 0).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(mockBreeds.map { it.toDomain() }, (result as Resource.Success).data)
    }

    @Test
    fun `getBreedImages should return images for breed`() = runTest {
        // Given
        val breedId = "1"
        val mockImages = listOf("image1.jpg", "image2.jpg")
        val mockEntity = CatBreedEntity(
            id = breedId,
            index = 1,
            name = "Siamese",
            isFavorite = false,
            images = mockImages,
            description = "The Siamese cat is one of the first distinctly recognized breeds of Asian cat.",
            temperament = "Active, Intelligent, Social",
            origin = "Thailand",
            lifeSpan = "12 - 15 years",
            weight = "8 - 15 pounds",
            hypoallergenic = 1,
            affectionLevel = 5,
            childFriendly = 4,
            strangerFriendly = 3,
            wikipediaUrl = "https://en.wikipedia.org/wiki/Siamese_cat"
        )

        coEvery { dao.getBreedImagesById(breedId) } returns flowOf(mockImages)
        coEvery { api.searchImages(breedIds = breedId) } returns emptyList()
        coEvery { db.withTransaction(any<suspend () -> Any>()) } coAnswers { firstArg<suspend () -> Any>().invoke() }
        coEvery { dao.updateBreedImagesById(any(), any()) } returns Unit

        // When
        val result = repository.getBreedImages(breedId).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(mockImages, (result as Resource.Success).data)
    }

    @Test
    fun `getCatBreedById should return breed from database`() = runTest {
        // Given
        val breedId = "1"
        val mockBreed = CatBreedEntity(
            id = breedId,
            index = 1,
            name = "Siamese",
            isFavorite = false,
            images = listOf("siamese1.jpg", "siamese2.jpg"),
            description = "The Siamese cat is one of the first distinctly recognized breeds of Asian cat.",
            temperament = "Active, Intelligent, Social",
            origin = "Thailand",
            lifeSpan = "12 - 15 years",
            weight = "8 - 15 pounds",
            hypoallergenic = 1,
            affectionLevel = 5,
            childFriendly = 4,
            strangerFriendly = 3,
            wikipediaUrl = "https://en.wikipedia.org/wiki/Siamese_cat"
        )
        coEvery { dao.getBreedById(breedId) } returns mockBreed

        // When
        val result = repository.getCatBreedById(breedId).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(mockBreed.toDomain(), (result as Resource.Success).data)
    }

    @Test
    fun `getCatBreeds should handle pagination correctly`() = runTest {
        // Given
        val limit = 10
        val page = 2
        val mockBreeds = listOf(
            CatBreedEntity(
                id = "3",
                index = 3,
                name = "Bengal",
                isFavorite = false,
                images = listOf("bengal1.jpg"),
                description = "Bengal cats are athletic and agile.",
                temperament = "Active, Energetic",
                origin = "United States",
                lifeSpan = "12 - 16 years",
                weight = "6 - 12 pounds",
                hypoallergenic = 0,
                affectionLevel = 4,
                childFriendly = 4,
                strangerFriendly = 3,
                wikipediaUrl = "https://en.wikipedia.org/wiki/Bengal_cat"
            )
        )
        coEvery { dao.getBreeds() } returns flowOf(mockBreeds)
        coEvery { api.getCatBreeds(limit, page) } returns emptyList()
        coEvery { db.withTransaction(any<suspend () -> Any>()) } coAnswers { firstArg<suspend () -> Any>().invoke() }
        coEvery { dao.insertBreeds(any()) } returns Unit

        // When
        val result = repository.getCatBreeds(limit, page).first()

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(mockBreeds.map { it.toDomain() }, (result as Resource.Success).data)
    }

    @Test
    fun `searchBreeds should handle empty search results`() = runTest {
        // Given
        val query = "NonExistentBreed"
        coEvery { dao.searchByName(query) } returns flowOf(emptyList())
        coEvery { api.searchBreeds(query, any()) } returns emptyList()
        coEvery { db.withTransaction(any<suspend () -> Any>()) } coAnswers { firstArg<suspend () -> Any>().invoke() }
        coEvery { dao.insertBreeds(any()) } returns Unit

        // When
        val result = repository.searchBreeds(query, 0).first()

        // Then
        assertTrue(result is Resource.Success)
        (result as Resource.Success).data?.let { assertTrue(it.isEmpty()) }
    }

    @Test
    fun `updateFavoriteStatus should handle database error`() = runTest {
        // Given
        val breedId = "1"
        val isFavorite = true
        val errorMessage = "Database error occurred"
        coEvery { dao.updateFavStatus(breedId, isFavorite) } throws Exception(errorMessage)

        // When
        val result = repository.updateFavoriteStatus(breedId, isFavorite).first()

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
        assertEquals(false, result.data)
    }

    @Test
    fun `getCatBreedById should handle database error`() = runTest {
        // Given
        val breedId = "1"
        coEvery { dao.getBreedById(breedId) } throws Exception("Database error")

        // When
        val result = repository.getCatBreedById(breedId).first()

        // Then
        assertTrue(result is Resource.Loading)
        val finalResult = repository.getCatBreedById(breedId).first()
        assertTrue(finalResult is Resource.Loading)
        assertEquals(false, (finalResult as Resource.Loading).isLoading)
    }
}
