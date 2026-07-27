package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.MainViewModel
import com.example.ui.navigation.MainAppNavigation
import com.example.ui.theme.MemoriaCardsTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeOverride by mainViewModel.themeOverride.collectAsState()

            MemoriaCardsTheme(darkThemeOverride = themeOverride) {
                MainAppNavigation(viewModel = mainViewModel)
            }
        }
    }
}
