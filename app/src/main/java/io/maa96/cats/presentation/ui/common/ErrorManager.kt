package io.maa96.cats.presentation.ui.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import io.maa96.cats.R

/**
 * Centralized error management for the application.
 * This class provides consistent error handling and display across all screens.
 */
object ErrorManager {
    /**
     * Displays an error message as a snackbar with retry option.
     * This is suitable for network errors or other recoverable errors.
     */
    @Composable
    fun ShowErrorSnackbar(
        errorMessage: String?,
        onDismiss: () -> Unit,
        onRetry: () -> Unit,
        snackbarHostState: SnackbarHostState
    ) {
        // Extract string resources
        val timeoutMessage = stringResource(R.string.error_timeout)
        val noInternetMessage = stringResource(R.string.error_no_internet)
        val serverErrorMessage = stringResource(R.string.error_server)
        val genericErrorMessage = stringResource(R.string.error_refresh_generic)
        val retryLabel = stringResource(R.string.retry)

        errorMessage?.let {
            // Format the error message to be user-friendly
            val userFriendlyMessage = when {
                it.contains("timeout", ignoreCase = true) -> timeoutMessage
                it.contains("host", ignoreCase = true) ||
                    it.contains("internet", ignoreCase = true) -> noInternetMessage
                it.contains("server", ignoreCase = true) || it.contains("500", ignoreCase = true) -> serverErrorMessage
                else -> genericErrorMessage
            }

            LaunchedEffect(errorMessage) {
                val result = snackbarHostState.showSnackbar(
                    message = userFriendlyMessage,
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

    /**
     * Formats an error message to be user-friendly based on the error type.
     * This can be used when you need the error message without displaying it.
     */
    fun formatErrorMessage(error: String, context: android.content.Context): String = when {
        error.contains("timeout", ignoreCase = true) ->
            context.getString(R.string.error_timeout)
        error.contains("host", ignoreCase = true) || error.contains("internet", ignoreCase = true) ->
            context.getString(R.string.error_no_internet)
        error.contains("server", ignoreCase = true) || error.contains("500", ignoreCase = true) ->
            context.getString(R.string.error_server)
        else -> context.getString(R.string.error_refresh_generic)
    }
}
