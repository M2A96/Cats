package io.maa96.cats.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.maa96.cats.presentation.theme.CatsTheme

@Composable
fun CatsApp() {
    CatsTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            catsNavGraph(navController)
        }
    }
}