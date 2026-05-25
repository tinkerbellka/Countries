package dem.alena.countries.ui.screen.detail

import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.data.repository.PersonalDataRepository
import dem.alena.countries.data.repository.ProfilesRepository
import dem.alena.countries.testutil.FakeCollectionsDao
import dem.alena.countries.testutil.FakeCountriesApi
import dem.alena.countries.testutil.FakeCountriesCacheDao
import dem.alena.countries.testutil.FakeCountriesSettingsRepository
import dem.alena.countries.testutil.FakeCountryNotesDao
import dem.alena.countries.testutil.FakeFavouritesDao
import dem.alena.countries.testutil.FakeUserProfileDao
import dem.alena.countries.testutil.FakeViewHistoryDao
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

    private fun createViewModel(api: FakeCountriesApi): CountryDetailViewModel {
        val settings = FakeCountriesSettingsRepository()
        val profiles = ProfilesRepository(FakeUserProfileDao(), settings)
        val personal = PersonalDataRepository(
            FakeViewHistoryDao(),
            FakeCountryNotesDao(),
            FakeCollectionsDao()
        )
        val repository = CountriesRepository(api, FakeFavouritesDao(), FakeCountriesCacheDao(), settings)
        return CountryDetailViewModel(repository, personal, profiles)
    }

    @Test
    fun load_success_setsCountryAndClearsError() = runTest {
        val api = FakeCountriesApi().apply {
            setCountrySuccess("JPN", testCountry(code = "JPN", name = "Japan"))
        }
        val viewModel = createViewModel(api)
        viewModel.load("JPN")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.error)
        assertEquals("JPN", viewModel.uiState.country?.code)
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
        val viewModel = createViewModel(api)
        viewModel.load("FRA")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.error)

        viewModel.retry()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.error)
        assertEquals("FRA", viewModel.uiState.country?.code)
    }
}
