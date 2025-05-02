package io.maa96.cats.presentation.ui.home

import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.maa96.cats.R
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.presentation.theme.CatsTheme
import io.maa96.cats.presentation.ui.DynamicAsyncImage

@Composable
fun HomeScreen(
    state: HomeScreenState,
    modifier: Modifier = Modifier,
    onEvent: (HomeScreenEvent) -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            HomeAppBar(
                onFavoriteClick = { onEvent(HomeScreenEvent.ShowFavorites) },
                showingFavoritesOnly = state.showingFavoritesOnly
            )
        },
        floatingActionButton = {
            ThemeToggleFab(
                onToggleTheme = { onEvent(HomeScreenEvent.ToggleTheme) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { query ->
                    Log.d("HomeScreen", "SearchBar query changed: $query")
                    onEvent(HomeScreenEvent.OnSearchQueryChange(query))
                }
            )

            when {
                state.isLoading -> {
                    ShimmerCatBreedList()
                }
                state.error != null && state.breeds.isEmpty() -> {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(HomeScreenEvent.Refresh) }
                    )
                }
                state.filteredBreeds.isEmpty() && state.searchQuery.isNotBlank() -> {
                    EmptySearchResult(query = state.searchQuery)
                }
                else -> {
                    CatBreedList(
                        breeds = if (state.showingFavoritesOnly) state.filteredBreeds else state.breeds,
                        isLoadingMore = state.isLoadingMore,
                        onBreedClick = onNavigateToDetails,
                        onFavoriteToggle = { breedId, isFav ->
                            onEvent(HomeScreenEvent.ToggleFavorite(breedId, isFav))
                        },
                        onLoadMore = {
                            if (!state.isLoading && !state.isLoadingMore && state.hasMoreData &&
                                !state.showingFavoritesOnly
                            ) {
                                onEvent(HomeScreenEvent.LoadMoreBreeds)
                            }
                        }
                    )

                    if (state.error != null && !state.isLoading) {
                        NetworkErrorSnackbar(
                            errorMessage = state.error,
                            onDismiss = { onEvent(HomeScreenEvent.ClearError) },
                            onRetry = { onEvent(HomeScreenEvent.Refresh) },
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showingFavoritesOnly: Boolean = false
) {
    TopAppBar(
        title = {
            Text(
                text = if (showingFavoritesOnly) {
                    stringResource(R.string.favorites)
                } else {
                    stringResource(R.string.title)
                },
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        actions = {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (showingFavoritesOnly) Icons.Default.List else Icons.Default.Favorite,
                    contentDescription = if (showingFavoritesOnly) {
                        stringResource(R.string.show_all)
                    } else {
                        stringResource(R.string.favorites)
                    },
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier
    )
}

@Composable
fun ThemeToggleFab(
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onToggleTheme,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = stringResource(R.string.toggle_theme)
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(stringResource(R.string.search_cat_breeds))
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
    }
}

@Composable
fun CatBreedList(
    breeds: List<Cat>,
    isLoadingMore: Boolean,
    onBreedClick: (String) -> Unit,
    onFavoriteToggle: (String, Boolean) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        state = lazyListState
    ) {
        items(
            items = breeds,
            key = { it.id }
        ) { breed ->
            CatBreedCard(
                breed = breed,
                onClick = { onBreedClick(breed.id) },
                onFavoriteClick = { onFavoriteToggle(breed.id, breed.isFavorite.not()) }
            )

            if (breed == breeds.lastOrNull()) {
                LaunchedEffect(key1 = true) {
                    onLoadMore()
                }
            }
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
//    LaunchedEffect(lazyListState, searchQuery) {
//        snapshotFlow { lazyListState.layoutInfo }.collect { layoutInfo ->
//            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
//            if (lastVisibleItem != null && lastVisibleItem.index >= breeds.size - 2 &&
//                !isLoadingMore && breeds.isNotEmpty() && searchQuery.isBlank()
//            ) {
//                Log.d("CatBreedList", "Triggering onLoadMore")
//                onLoadMore()
//            }
//        }
//    }
}

@Composable
fun CatBreedCard(
    breed: Cat,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    breed.images?.get(0)?.let {
                        DynamicAsyncImage(
                            imageUrl = it,
                            contentDescription = "Cat Image",
                            modifier = modifier,
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = breed.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${stringResource(R.string.temperament)}: ${breed.temperament}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${stringResource(R.string.origin)}: ${breed.origin}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = if (breed.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (breed.isFavorite) {
                        stringResource(R.string.remove_from_favorites)
                    } else {
                        stringResource(R.string.add_to_favorites)
                    },
                    tint = if (breed.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ShimmerCatBreedList(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(5) {
            ShimmerCatBreedItem()
        }
    }
}

@Composable
fun ShimmerCatBreedItem(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, delayMillis = 300)
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(brush)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(15.dp)
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(15.dp)
                        .background(brush)
                )
            }
        }
    }
}

@Composable
fun EmptySearchResult(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.no_results_found, query),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.adjust_search_tip),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.connection_error),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.try_again))
        }
    }
}

@Composable
fun NetworkErrorSnackbar(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val retryLabel = stringResource(R.string.retry)

    errorMessage?.let {
        LaunchedEffect(errorMessage) {
            val result = snackbarHostState.showSnackbar(
                message = it,
                actionLabel = retryLabel,
                duration = SnackbarDuration.Long
            )
            when (result) {
                SnackbarResult.ActionPerformed -> onRetry()
                SnackbarResult.Dismissed -> onDismiss()
            }
        }
    }
}

@Composable
fun StaleBanner(
    lastUpdated: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.showing_cached_data, lastUpdated),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            TextButton(
                onClick = onRefresh,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.refresh),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun HomeAppBarPreview() {
    CatsTheme {
        HomeAppBar(
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    CatsTheme {
        SearchBar(
            query = "Bengal",
            onQueryChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CatBreedCardPreview() {
    CatsTheme {
        CatBreedCard(
            breed = Cat(
                id = "siam",
                name = "Siamese",
                images = listOf("https://cdn2.thecatapi.com/images/xnsqonbjW.jpg"),
                temperament = "Curious, Intelligent, Social",
                origin = "Thailand",
                description = "Bengals are a lot of fun to live with, but they're definitely not the cat for everyone, or for first-time cat owners. Extremely intelligent, curious and active, they demand a lot of interaction and woe betide the owner who doesn't provide it.",
                lifeSpan = "12-16 years",
                weight = "8-15 lbs",
                hypoallergenic = 0,
                affectionLevel = 3,
                childFriendly = 3,
                strangerFriendly = 3,
                wikipediaUrl = "https://en.wikipedia.org/wiki/Bengal_cat",
                isFavorite = true
            ),
            onClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun HomeScreenPreview() {
    CatsTheme {
        HomeScreen(
            state = HomeScreenState(
                searchQuery = "Bengal",
                filteredBreeds = listOf(
                    Cat(
                        id = "beng",
                        name = "Bengal",
                        images = listOf("https://cdn2.thecatapi.com/images/xnsqonbjW.jpg"),
                        temperament = "Alert, Agile, Energetic",
                        description = "Bengals are a lot of fun to live with, but they're definitely not the cat for everyone, or for first-time cat owners. Extremely intelligent, curious and active, they demand a lot of interaction and woe betide the owner who doesn't provide it.",
                        origin = "United States",
                        lifeSpan = "12-16 years",
                        weight = "8-15 lbs",
                        hypoallergenic = 0,
                        affectionLevel = 3,
                        childFriendly = 3,
                        strangerFriendly = 3,
                        wikipediaUrl = "https://en.wikipedia.org/wiki/Bengal_cat",
                        isFavorite = true
                    ),
                    Cat(
                        id = "siam",
                        name = "Siamese",
                        images = listOf("https://cdn2.thecatapi.com/images/xnsqonbjW.jpg"),
                        temperament = "Curious, Intelligent, Social",
                        origin = "Thailand",
                        description = "Bengals are a lot of fun to live with, but they're definitely not the cat for everyone, or for first-time cat owners. Extremely intelligent, curious and active, they demand a lot of interaction and woe betide the owner who doesn't provide it.",
                        lifeSpan = "12-16 years",
                        weight = "8-15 lbs",
                        hypoallergenic = 0,
                        affectionLevel = 3,
                        childFriendly = 3,
                        strangerFriendly = 3,
                        wikipediaUrl = "https://en.wikipedia.org/wiki/Bengal_cat",
                        isFavorite = true
                    )
                )
            ),
            onEvent = {},
            onNavigateToDetails = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptySearchResultPreview() {
    CatsTheme {
        EmptySearchResult(query = "xyz")
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorStatePreview() {
    CatsTheme {
        ErrorState(
            message = "Failed to load cat breeds. Please check your internet connection.",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ShimmerCatBreedListPreview() {
    CatsTheme {
        ShimmerCatBreedList()
    }
}
