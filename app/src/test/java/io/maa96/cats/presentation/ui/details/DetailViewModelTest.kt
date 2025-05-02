package io.maa96.cats.presentation.ui.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.usecase.GetBreedImagesUseCase
import io.maa96.cats.domain.usecase.GetCatBreedByIdUseCase
import io.maa96.cats.domain.usecase.UpdateFavoriteStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class DetailViewModelTest {
    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DetailViewModel
    private val getCatBreedByIdUseCase: GetCatBreedByIdUseCase = mockk()
    private val getBreedImagesUseCase: GetBreedImagesUseCase = mockk()
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase = mockk()
    private val savedStateHandle = SavedStateHandle(mapOf("breedId" to "1"))

    private val mockCat = Cat(
        id = "1",
        name = "Siamese",
        description = "The Siamese cat is one of the first distinctly recognized breeds of Asian cat.",
        temperament = "Active, Intelligent, Social",
        origin = "Thailand",
        lifeSpan = "12 - 15 years",
        weight = "8 - 15 pounds",
        wikipediaUrl = "https://en.wikipedia.org/wiki/Siamese_cat",
        images = listOf("siamese1.jpg", "siamese2.jpg"),
        isFavorite = false,
        hypoallergenic = 0,
        affectionLevel = 5,
        childFriendly = 4,
        strangerFriendly = 3
    )

    private val mockImages = listOf("image1.jpg", "image2.jpg", "image3.jpg")

    @Before
    fun setup() {
        coEvery { getCatBreedByIdUseCase(any()) } returns flowOf(Resource.Success(mockCat))
        coEvery { getBreedImagesUseCase(any()) } returns flowOf(Resource.Success(mockImages))
        coEvery { updateFavoriteStatusUseCase(any(), any()) } returns flowOf(Resource.Success(true))

        viewModel = DetailViewModel(
            getCatBreedByIdUseCase,
            getBreedImagesUseCase,
            updateFavoriteStatusUseCase,
            savedStateHandle
        )
    }

    @Test
    fun `initial state should load breed details and images`() = runTest {
        val currentState = viewModel.uiState.value

        assertFalse(currentState.isLoading)
        assertEquals(mockCat, currentState.catDetail)
        assertEquals(mockImages, currentState.catDetail?.images)
        assertEquals(0, currentState.selectedImageIndex)
    }

    @Test
    fun `selecting image should update selected index`() = runTest {
        val selectedIndex = 2

        viewModel.onEvent(DetailScreenEvent.SelectImage(selectedIndex))

        assertEquals(selectedIndex, viewModel.uiState.value.selectedImageIndex)
    }

    @Test
    fun `toggling favorite should update breed favorite status`() = runTest {
        viewModel.onEvent(DetailScreenEvent.ToggleFavorite(mockCat))

        coVerify { updateFavoriteStatusUseCase(mockCat.id, !mockCat.isFavorite) }

        // Verify the UI state is updated
        val updatedCat = viewModel.uiState.value.catDetail
        assertEquals(!mockCat.isFavorite, updatedCat?.isFavorite)
    }

    @Test
    fun `refresh should reload breed details`() = runTest {
        viewModel.onEvent(DetailScreenEvent.Refresh)

        coVerify {
            getCatBreedByIdUseCase("1")
        }
    }

    @Test
    fun `error in loading breed details should update error state`() = runTest {
        val errorMessage = "Failed to load breed details"
        coEvery { getCatBreedByIdUseCase(any()) } returns flowOf(Resource.Error(errorMessage, null))

        // Recreate viewModel to trigger error state
        viewModel = DetailViewModel(
            getCatBreedByIdUseCase,
            getBreedImagesUseCase,
            updateFavoriteStatusUseCase,
            savedStateHandle
        )

        val currentState = viewModel.uiState.value
        assertEquals(errorMessage, currentState.error)
        assertFalse(currentState.isLoading)
    }

    @Test
    fun `error in loading images should update error state`() = runTest {
        val errorMessage = "Failed to load images"
        coEvery { getBreedImagesUseCase(any()) } returns flowOf(Resource.Error(errorMessage, null))

        // Recreate viewModel to trigger error state
        viewModel = DetailViewModel(
            getCatBreedByIdUseCase,
            getBreedImagesUseCase,
            updateFavoriteStatusUseCase,
            savedStateHandle
        )

        val currentState = viewModel.uiState.value
        assertEquals(errorMessage, currentState.error)
        assertFalse(currentState.isLoading)
    }
}
