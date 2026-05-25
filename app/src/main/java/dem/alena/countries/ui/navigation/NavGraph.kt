package dem.alena.countries.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dem.alena.countries.ui.screen.collections.CollectionDetailScreen
import dem.alena.countries.ui.screen.collections.CollectionDetailViewModel
import dem.alena.countries.ui.screen.collections.CollectionsScreen
import dem.alena.countries.ui.screen.collections.CollectionsViewModel
import dem.alena.countries.ui.screen.detail.CountryDetailScreen
import dem.alena.countries.ui.screen.detail.CountryDetailViewModel
import dem.alena.countries.ui.screen.history.HistoryScreen
import dem.alena.countries.ui.screen.history.HistoryViewModel
import dem.alena.countries.ui.screen.list.CountriesListScreen
import dem.alena.countries.ui.screen.list.CountriesViewModel
import dem.alena.countries.ui.screen.list.FavouritesScreen
import dem.alena.countries.ui.screen.settings.SettingsScreen
import dem.alena.countries.ui.screen.settings.SettingsViewModel

sealed class MainTab(val route: String, val label: String) {
    data object List : MainTab("list", "Страны")
    data object Collections : MainTab("collections", "Коллекции")
    data object History : MainTab("history", "История")
    data object Settings : MainTab("settings", "Настройки")
}

@Composable
fun CountriesAppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val tabs = listOf(
        MainTab.List,
        MainTab.Collections,
        MainTab.History,
        MainTab.Settings
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in tabs.map { it.route }

    val countriesViewModel: CountriesViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(tab.label) },
                            icon = { Text(tab.label.take(1)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.List.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.List.route) {
                CountriesListScreen(
                    viewModel = countriesViewModel,
                    onCountryClick = { code -> navController.navigate("detail/$code") },
                    onFavouritesClick = { navController.navigate("favourites") }
                )
            }

            composable("favourites") {
                FavouritesScreen(
                    viewModel = countriesViewModel,
                    onCountryClick = { code -> navController.navigate("detail/$code") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(MainTab.Collections.route) {
                val vm: CollectionsViewModel = hiltViewModel()
                CollectionsScreen(
                    viewModel = vm,
                    onCollectionClick = { id -> navController.navigate("collection/$id") }
                )
            }

            composable("collection/{collectionId}") {
                val vm: CollectionDetailViewModel = hiltViewModel()
                CollectionDetailScreen(
                    viewModel = vm,
                    onCountryClick = { code -> navController.navigate("detail/$code") }
                )
            }

            composable(MainTab.History.route) {
                val vm: HistoryViewModel = hiltViewModel()
                HistoryScreen(
                    viewModel = vm,
                    onCountryClick = { code -> navController.navigate("detail/$code") }
                )
            }

            composable(MainTab.Settings.route) {
                val vm: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = vm)
            }

            composable("detail/{code}") { entry ->
                val code = entry.arguments?.getString("code") ?: return@composable
                val vm: CountryDetailViewModel = hiltViewModel()
                CountryDetailScreen(code = code, viewModel = vm, navController = navController)
            }
        }
    }
}
