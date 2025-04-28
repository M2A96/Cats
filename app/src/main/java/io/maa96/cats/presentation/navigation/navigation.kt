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


sealed class Screen(val route: String){
    data object Home: Screen("home")
    data object Detail: Screen("details/{catId}") {
        fun createRoute(catId: String) = "details/$catId"
    }
}

fun NavGraphBuilder.CatsNavGraph(navController: NavHostController){
    composable(Screen.Home.route){
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsState()
        HomeScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateToDetails = { characterId ->
                navController.navigate(Screen.Detail.createRoute(characterId))
            }

        )
    }

    composable(Screen.Detail.route){
        val viewModel: DetailViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsState()
        DetailScreen(
            state = state,
            onEvent = viewModel::onEvent
        )
    }
}