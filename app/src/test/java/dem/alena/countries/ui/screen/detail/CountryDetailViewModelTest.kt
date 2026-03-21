package dem.alena.countries.ui.screen.detail

import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.testutil.FakeCountriesApi
import dem.alena.countries.testutil.FakeFavouritesDao
import dem.alena.countries.testutil.MainDispatcherRule
import dem.alena.countries.testutil.testCountry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun load_success_setsCountryAndClearsError() = runTest {
        val api = FakeCountriesApi().apply {
            setCountrySuccess("JPN", testCountry(code = "JPN", name = "Japan"))
        }
        val viewModel = CountryDetailViewModel(CountriesRepository(api, FakeFavouritesDao()))

        viewModel.load("JPN")
        advanceUntilIdle()

        assertFalse(viewModel.error)
        assertEquals("JPN", viewModel.country?.code)
    }

    @Test
    fun retry_afterError_usesLastCodeAndLoadsAgain() = runTest {
        val api = object : FakeCountriesApi() {
            private var first = true
            override suspend fun getCountryByCode(code: String, fields: String) =
                if (first) {
                    first = false
                    throw IllegalStateException("first fail")
                } else {
                    testCountry(code = code, name = "Recovered")
                }
        }
        val viewModel = CountryDetailViewModel(CountriesRepository(api, FakeFavouritesDao()))

        viewModel.load("FRA")
        advanceUntilIdle()
        assertTrue(viewModel.error)

        viewModel.retry()
        advanceUntilIdle()

        assertFalse(viewModel.error)
        assertEquals("FRA", viewModel.country?.code)
    }

    @Test
    fun retry_withoutPreviousLoad_doesNothing() = runTest {
        val viewModel = CountryDetailViewModel(
            CountriesRepository(FakeCountriesApi(), FakeFavouritesDao())
        )

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(null, viewModel.country)
        assertFalse(viewModel.error)
    }
}

