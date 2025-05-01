package io.maa96.cats.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import io.maa96.cats.R

/**
 * A wrapper around AsyncImage which provides proper loading, error states,
 * and automatically determines the appropriate content scaling
 */
@Composable
fun DynamicAsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Int = R.drawable.ic_launcher_background,
    contentScale: ContentScale = ContentScale.Crop
) {
    val isLocalInspection = LocalInspectionMode.current

    if (isLocalInspection) {
        // In preview mode, just show the placeholder
        Image(
            painter = painterResource(id = placeholder),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // Normal runtime behavior
        val painter = rememberAsyncImagePainter(
            model = imageUrl
        )
        var loadState by remember { mutableStateOf<AsyncImagePainter.State>(painter.state) }

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            // Track the current state
            loadState = painter.state

            // Show the actual image
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )

            // Handle different states
            when (loadState) {
                is AsyncImagePainter.State.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                is AsyncImagePainter.State.Error -> {
                    // Show error placeholder
                    Image(
                        painter = painterResource(id = placeholder),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale
                    )
                }
                is AsyncImagePainter.State.Success -> {
                    // Success case - already showing the image
                }
                else -> {
                    // Handle any other state
                }
            }
        }
    }
}