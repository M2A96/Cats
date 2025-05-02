package io.maa96.cats.presentation.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.usecase.GetCatBreedsUseCase
import io.maa96.cats.domain.usecase.SearchBreedsUseCase
import io.maa96.cats.domain.usecase.UpdateFavoriteStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class HomeViewModelTest {
    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private val getCatBreedsUseCase: GetCatBreedsUseCase = mockk()
    private val searchBreedsUseCase: SearchBreedsUseCase = mockk()
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase = mockk()
    private val searchDebouncer: SearchDebouncer = mockk(relaxed = true)
    private val PAGE_SIZE = 10

    private val mockCats = listOf(
        Cat(
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
        ),
        Cat(
            id = "2",
            name = "Persian",
            description = "The Persian cat is a long-haired breed of cat characterized by its round face.",
            temperament = "Gentle, Quiet, Affectionate",
            origin = "Iran (Persia)",
            lifeSpan = "10 - 15 years",
            weight = "7 - 12 pounds",
            wikipediaUrl = "https://en.wikipedia.org/wiki/Persian_cat",
            images = listOf("persian1.jpg", "persian2.jpg"),
            isFavorite = true,
            hypoallergenic = 0,
            affectionLevel = 4,
            childFriendly = 3,
            strangerFriendly = 2
        )
    )

    @Before
    fun setup() {
        coEvery { getCatBreedsUseCase(any(), any()) } returns flowOf(Resource.Success(mockCats))
        coEvery { searchBreedsUseCase(any(), any()) } returns flowOf(Resource.Success(mockCats))
        coEvery { updateFavoriteStatusUseCase(any(), any()) } returns flowOf(Resource.Success(true))

        viewModel = HomeViewModel(
            getCatBreedsUseCase,
            searchBreedsUseCase,
            updateFavoriteStatusUseCase,
            searchDebouncer
        )
    }

    @Test
    fun `initial state should load breeds successfully`() = runTest {
        // Initial state should be loaded in setup()
        val currentState = viewModel.uiState.value

        assertFalse(currentState.isLoading)
        assertEquals(mockCats, currentState.breeds)
        assertEquals("", currentState.searchQuery)
    }

    @Test
    fun `toggling favorite should update breed status`() = runTest {
        val breed = mockCats[0]

        viewModel.onEvent(HomeScreenEvent.ToggleFavorite(breed))

        coVerify { updateFavoriteStatusUseCase(breed.id, !breed.isFavorite) }
    }

    @Test
    fun `search query should trigger search`() = runTest {
        val searchQuery = "Siamese"
        val searchResults = listOf(mockCats[0])

        coEvery { searchBreedsUseCase(searchQuery, 0) } returns flowOf(Resource.Success(searchResults))

        viewModel.onEvent(HomeScreenEvent.OnSearchQueryChange(searchQuery))

        // Verify search debouncer was called with query
        coVerify { searchDebouncer.setQuery(searchQuery) }
    }

    @Test
    fun `show favorites should filter breeds`() = runTest {
        viewModel.onEvent(HomeScreenEvent.ShowFavorites)

        val currentState = viewModel.uiState.value
        assertTrue(currentState.showingFavoritesOnly)

        // When showing favorites, only Persian cat should be visible
        val visibleBreeds = currentState.filteredBreeds
        assertEquals(1, visibleBreeds.size)
        assertEquals("Persian", visibleBreeds[0].name)
    }

    @Test
    fun `refresh should reload breeds`() = runTest {
        viewModel.onEvent(HomeScreenEvent.Refresh)

        coVerify { getCatBreedsUseCase(PAGE_SIZE, 1) }
    }

    @Test
    fun `load more should fetch next page`() = runTest {
        viewModel.onEvent(HomeScreenEvent.LoadMoreBreeds)

        // Verify that the second page is requested
        coVerify { getCatBreedsUseCase(PAGE_SIZE, 2) }
    }

    @Test
    fun `error should be cleared when requested`() = runTest {
        // Simulate an error state
        coEvery { getCatBreedsUseCase(any(), any()) } returns flowOf(Resource.Error("Test error", null))
        viewModel =
            HomeViewModel(getCatBreedsUseCase, searchBreedsUseCase, updateFavoriteStatusUseCase, searchDebouncer)

        // Clear the error
        viewModel.onEvent(HomeScreenEvent.ClearError)

        // Verify error is cleared
        val currentState = viewModel.uiState.value
        assertEquals(null, currentState.error)
    }
}
