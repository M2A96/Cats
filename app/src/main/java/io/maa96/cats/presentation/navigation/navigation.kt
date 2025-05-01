package io.maa96.cats.presentation.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.maa96.cats.presentation.ui.details.DetailScreen
import io.maa96.cats.presentation.ui.details.DetailViewModel
import io.maa96.cats.presentation.ui.home.HomeScreen
import io.maa96.cats.presentation.ui.home.HomeViewModel
import io.maa96.cats.presentation.ui.webview.WebViewScreen
import io.maa96.cats.presentation.ui.webview.WebViewViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Detail : Screen("detail/{breedId}") {
        fun createRoute(catId: String) = "detail/$catId"
    }
    data object WebView : Screen("webview?url={url}&title={title}") {
        fun createRoute(url: String, title: String): String = "webview?url=$url&title=$title"
    }
}

fun NavGraphBuilder.catsNavGraph(navController: NavHostController) {
    composable(Screen.Home.route) {
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsState()
        HomeScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateToDetails = {
                navController.navigate(Screen.Detail.createRoute(it))
            }

        )
    }

    composable(Screen.Detail.route) {
        val viewModel: DetailViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsState()
        DetailScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToWebView = { url, title ->
                navController.navigate(Screen.WebView.createRoute(url, title))
            }
        )
    }

    composable(
        route = Screen.WebView.route
    ) {
        val viewModel: WebViewViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsState()
        WebViewScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
}
