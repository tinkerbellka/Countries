package dem.alena.countries.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.testutil.AndroidFakeCountriesApi
import dem.alena.countries.testutil.AndroidFakeCountriesSettingsRepository
import dem.alena.countries.testutil.AndroidFakeFavouritesDao
import dem.alena.countries.testutil.androidTestCountry
import dem.alena.countries.ui.screen.list.CountriesListScreen
import dem.alena.countries.ui.screen.list.CountriesViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CountriesUiIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun listClick_navigatesToDetailWithCorrectId() {
        val api = AndroidFakeCountriesApi().apply {
            enqueueGetAllSuccess(listOf(androidTestCountry(code = "BRA", name = "Brazil")))
        }
        val viewModel = CountriesViewModel(
            CountriesRepository(api, AndroidFakeFavouritesDao()),
            AndroidFakeCountriesSettingsRepository()
        )

        composeRule.setContent {
            TestNavContent(viewModel = viewModel)
        }

        composeRule.onNodeWithText("Brazil").assertIsDisplayed()
        composeRule.onNodeWithText("Brazil").performClick()
        composeRule.onNodeWithText("DETAIL_BRA").assertIsDisplayed()
    }

    @Test
    fun errorThenRetry_showsSuccessState() {
        val api = AndroidFakeCountriesApi().apply {
            enqueueGetAllError(IllegalStateException("network down"))
            enqueueGetAllSuccess(listOf(androidTestCountry(code = "CAN", name = "Canada")))
        }
        val viewModel = CountriesViewModel(
            CountriesRepository(api, AndroidFakeFavouritesDao()),
            AndroidFakeCountriesSettingsRepository()
        )

        composeRule.setContent {
            CountriesListScreen(
                viewModel = viewModel,
                onCountryClick = {},
                onFavouritesClick = {}
            )
        }

        composeRule.onNodeWithText("Повторить").assertIsDisplayed()
        composeRule.onNodeWithText("Повторить").performClick()
        composeRule.onNodeWithText("Canada").assertIsDisplayed()
    }

    @Test
    fun emptyData_showsEmptyStateText() {
        val api = AndroidFakeCountriesApi().apply {
            enqueueGetAllSuccess(emptyList())
        }
        val viewModel = CountriesViewModel(
            CountriesRepository(api, AndroidFakeFavouritesDao()),
            AndroidFakeCountriesSettingsRepository()
        )

        composeRule.setContent {
            CountriesListScreen(
                viewModel = viewModel,
                onCountryClick = {},
                onFavouritesClick = {}
            )
        }

        composeRule.onNodeWithText("Ничего не найдено. Попробуйте другой запрос или фильтр.")
            .assertIsDisplayed()
    }
}

@Composable
private fun TestNavContent(viewModel: CountriesViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            CountriesListScreen(
                viewModel = viewModel,
                onCountryClick = { code -> navController.navigate("detail/$code") },
                onFavouritesClick = {}
            )
        }
        composable("detail/{code}") { backStackEntry ->
            Text("DETAIL_${backStackEntry.arguments?.getString("code").orEmpty()}")
        }
    }
}
