package io.maa96.cats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import io.maa96.cats.presentation.navigation.CatsApp
import io.maa96.cats.presentation.theme.CatsTheme
import io.maa96.cats.presentation.ui.home.HomeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeIsDark = viewModel.uiState.collectAsState().value.currentThemIsDark
            CatsTheme(darkTheme = themeIsDark) {
                CatsApp()
            }
        }
    }
}
