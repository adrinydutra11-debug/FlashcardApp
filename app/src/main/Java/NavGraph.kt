package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.MainViewModel
import com.example.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Decks", Icons.Default.Style)
    object Games : Screen("games/-1", "Jogos", Icons.Default.SportsEsports)
    object Tutor : Screen("tutor", "Tutora IA", Icons.Default.SmartToy)
    object Statistics : Screen("statistics", "Progresso", Icons.Default.BarChart)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}

@Composable
fun MainAppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Games,
        Screen.Tutor,
        Screen.Statistics,
        Screen.Settings
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        "games/{deckId}",
        Screen.Tutor.route,
        Screen.Statistics.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route ||
                                (screen == Screen.Games && currentRoute?.startsWith("games") == true)

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDeck = { deckId -> navController.navigate("deck_detail/$deckId") },
                    onNavigateToStudy = { deckId -> navController.navigate("study/${deckId ?: -1L}") },
                    onNavigateToGames = { deckId -> navController.navigate("games/${deckId ?: -1L}") }
                )
            }

            composable(
                route = "deck_detail/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.LongType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
                DeckDetailScreen(
                    deckId = deckId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onStudy = { id -> navController.navigate("study/$id") },
                    onPlayGames = { id -> navController.navigate("games/$id") }
                )
            }

            composable(
                route = "study/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.LongType })
            ) { backStackEntry ->
                val deckIdArg = backStackEntry.arguments?.getLong("deckId")
                val deckId = if (deckIdArg == -1L) null else deckIdArg
                StudyReviewScreen(
                    deckId = deckId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "games/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.LongType })
            ) { backStackEntry ->
                val deckIdArg = backStackEntry.arguments?.getLong("deckId")
                val deckId = if (deckIdArg == -1L) null else deckIdArg
                GamesHubScreen(
                    deckId = deckId,
                    viewModel = viewModel,
                    onNavigateToWordSearch = { id -> navController.navigate("game_word_search/${id ?: -1L}") },
                    onNavigateToQuiz = { id -> navController.navigate("game_quiz/${id ?: -1L}") },
                    onNavigateToFillBlanks = { id -> navController.navigate("game_fill_blanks/${id ?: -1L}") },
                    onNavigateToCustomGame = { navController.navigate("game_custom") }
                )
            }

            composable(
                route = "game_word_search/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.LongType })
            ) { backStackEntry ->
                val deckIdArg = backStackEntry.arguments?.getLong("deckId")
                val deckId = if (deckIdArg == -1L) null else deckIdArg
                WordSearchGameScreen(
                    deckId = deckId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "game_quiz/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.LongType })
            ) { backStackEntry ->
                val deckIdArg = backStackEntry.arguments?.getLong("deckId")
                val deckId = if (deckIdArg == -1L) null else deckIdArg
                QuizGameScreen(
                    deckId = deckId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "game_fill_blanks/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.LongType })
            ) { backStackEntry ->
                val deckIdArg = backStackEntry.arguments?.getLong("deckId")
                val deckId = if (deckIdArg == -1L) null else deckIdArg
                FillBlanksGameScreen(
                    deckId = deckId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("game_custom") {
                CustomGameBuilderScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onStartGame = { navController.navigate("game_quiz/-1") }
                )
            }

            composable(Screen.Tutor.route) {
                AiTutorScreen(viewModel = viewModel)
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsNotificationsScreen(viewModel = viewModel)
            }
        }
    }
}
