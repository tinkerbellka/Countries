package dem.alena.countries.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dem.alena.countries.ui.screen.detail.CountryDetailScreen
import dem.alena.countries.ui.screen.detail.CountryDetailViewModel
import dem.alena.countries.ui.screen.list.CountriesListScreen
import dem.alena.countries.ui.screen.list.CountriesViewModel
import dem.alena.countries.ui.screen.list.FavouritesScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    val countriesViewModel: CountriesViewModel = viewModel()

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

            val detailViewModel: CountryDetailViewModel = viewModel()

            CountryDetailScreen(
                code = code,
                viewModel = detailViewModel,
                navController = navController
            )
        }
    }
}
