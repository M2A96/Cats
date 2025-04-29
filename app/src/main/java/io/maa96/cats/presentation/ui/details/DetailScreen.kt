package io.maa96.cats.presentation.ui.details

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import io.maa96.cats.R
import io.maa96.cats.domain.model.Cat
import io.maa96.cats.presentation.theme.CatsTheme

@Composable
fun DetailScreen(
    state: DetailScreenState,
    modifier: Modifier = Modifier,
    onEvent: (DetailScreenEvent) -> Unit
) {
    Scaffold { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    ShimmerDetailScreen()
                }
                state.error != null -> {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(DetailScreenEvent.Refresh) }
                    )
                }
                state.catDetail != null -> {
                    DetailContent(
                        catDetail = state.catDetail,
                        selectedImageIndex = state.selectedImageIndex,
                        onBackClick = { onEvent(DetailScreenEvent.NavigateBack) },
                        onFavoriteClick = { onEvent(DetailScreenEvent.ToggleFavorite) },
                        onImageSelect = { index -> onEvent(DetailScreenEvent.SelectImage(index)) },
                        onWikipediaClick = { onEvent(DetailScreenEvent.OpenWikipedia) }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailContent(
    catDetail: Cat,
    selectedImageIndex: Int,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onImageSelect: (Int) -> Unit,
    onWikipediaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Main Image
            Image(
                painter = rememberAsyncImagePainter(
                    model = catDetail.images?.getOrNull(selectedImageIndex) ?: catDetail.images?.firstOrNull()
                ),
                contentDescription = "${catDetail.name} ${stringResource(R.string.cat_image)}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }

            // Favorite Button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = if (catDetail.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (catDetail.isFavorite)
                        stringResource(R.string.remove_from_favorites)
                    else
                        stringResource(R.string.add_to_favorites),
                    tint = if (catDetail.isFavorite) Color.Red else Color.White
                )
            }
        }

        // Image Carousel
        ImageCarousel(
            images = catDetail.images?: listOf(),
            selectedIndex = selectedImageIndex,
            onImageSelect = onImageSelect
        )

        // Detail Content
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = catDetail.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            DetailStatsRow(
                origin = catDetail.origin,
                lifespan = catDetail.lifeSpan,
                weight = catDetail.weight
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(title = stringResource(R.string.temperament))
            Text(
                text = catDetail.temperament,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(title = stringResource(R.string.description))
            Text(
                text = catDetail.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(title = stringResource(R.string.characteristics))
            CharacteristicItem(
                label = stringResource(R.string.hypoallergenic),
                value = if (catDetail.hypoallergenic == 0) stringResource(R.string.yes) else stringResource(R.string.no)
            )
            CharacteristicItem(
                label = stringResource(R.string.affection_level),
                value = getLevelText(catDetail.affectionLevel)
            )
            CharacteristicItem(
                label = stringResource(R.string.child_friendly),
                value = getLevelText(catDetail.childFriendly)
            )
            CharacteristicItem(
                label = stringResource(R.string.stranger_friendly),
                value = getLevelText(catDetail.strangerFriendly)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Wikipedia Button
            Button(
                onClick = onWikipediaClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.visit_wikipedia),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ImageCarousel(
    images: List<String>,
    selectedIndex: Int,
    onImageSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(images) { index, imageUrl ->
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (index == selectedIndex) 2.dp else 0.dp,
                        color = if (index == selectedIndex) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onImageSelect(index) }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun DetailStatsRow(
    origin: String,
    lifespan: String,
    weight: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem(
            label = "${stringResource(R.string.origin)}: $origin",
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = "${stringResource(R.string.lifespan)}: $lifespan",
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = "${stringResource(R.string.weight)}: $weight",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatItem(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun CharacteristicItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ShimmerDetailScreen(
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero image placeholder
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(brush)
        )

        // Image carousel placeholder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                Spacer(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush)
                )
            }
        }

        // Content placeholders
        Column(modifier = Modifier.padding(16.dp)) {
            // Title placeholder
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(32.dp)
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(brush)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section title placeholder
            Spacer(
                modifier = Modifier
                    .width(120.dp)
                    .height(24.dp)
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Text content placeholder
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(brush)
            )

            repeat(4) {
                Spacer(modifier = Modifier.height(8.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(brush)
                )
            }

            // More sections
            repeat(2) {
                Spacer(modifier = Modifier.height(24.dp))

                Spacer(
                    modifier = Modifier
                        .width(120.dp)
                        .height(24.dp)
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(8.dp))

                repeat(3) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Button placeholder
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(brush)
            )
        }
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
            imageVector = Icons.Default.ArrowBack,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.try_again))
        }
    }
}

// Helper function to convert level numbers to text descriptions
@Composable
fun getLevelText(level: Int): String {
    return when (level) {
        1 -> stringResource(R.string.very_low)
        2 -> stringResource(R.string.low)
        3 -> stringResource(R.string.medium)
        4 -> stringResource(R.string.high)
        5 -> stringResource(R.string.very_high)
        else -> stringResource(R.string.unknown)
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    CatsTheme {
        DetailScreen(
            state = DetailScreenState(
                catDetail = Cat(
                    id = "beng",
                    name = "Bengal",
                    images = listOf("", "", "", ""),
                    description = "Bengals are a lot of fun to live with, but they're definitely not the cat for everyone, or for first-time cat owners. Extremely intelligent, curious and active, they demand a lot of interaction and woe betide the owner who doesn't provide it.",
                    temperament = "Alert, Agile, Energetic, Demanding, Intelligent",
                    origin = "United States",
                    lifeSpan = "12-16 years",
                    weight = "8-15 lbs",
                    hypoallergenic = 0,
                    affectionLevel = 3,
                    childFriendly = 3,
                    strangerFriendly = 3,
                    wikipediaUrl = "https://en.wikipedia.org/wiki/Bengal_cat",
                    isFavorite = true
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ShimmerDetailScreenPreview() {
    CatsTheme {
        ShimmerDetailScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorStatePreview() {
    CatsTheme {
        ErrorState(
            message = "Failed to load cat breed details. Please check your internet connection.",
            onRetry = {}
        )
    }
}