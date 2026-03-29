package dem.alena.countries.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import dem.alena.countries.ui.screen.detail.CountryDetailScreen
import dem.alena.countries.ui.screen.detail.CountryDetailViewModel
import dem.alena.countries.ui.screen.list.CountriesListScreen
import dem.alena.countries.ui.screen.list.CountriesViewModel
import dem.alena.countries.ui.screen.list.FavouritesScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    val countriesViewModel: CountriesViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {

        composable("list") {
            CountriesListScreen(
                viewModel = countriesViewModel,
                onCountryClick = { code ->
                    navController.navigate("detail/$code")
                },
                onFavouritesClick = {
                    navController.navigate("favourites")
                }
            )
        }

        composable("favourites") {
            FavouritesScreen(
                viewModel = countriesViewModel,
                onCountryClick = { code ->
                    navController.navigate("detail/$code")
                }
            )
        }

        composable("detail/{code}") { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code")
                ?: return@composable

            val detailViewModel: CountryDetailViewModel = hiltViewModel()

            CountryDetailScreen(
                code = code,
                viewModel = detailViewModel,
                navController = navController
            )
        }
    }
}
