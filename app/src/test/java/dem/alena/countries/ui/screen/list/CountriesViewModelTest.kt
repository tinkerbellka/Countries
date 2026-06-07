package dem.alena.countries.ui.screen.list

import dem.alena.countries.data.preferences.CountriesSortOrder
import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.data.repository.PersonalDataRepository
import dem.alena.countries.data.repository.ProfilesRepository
import dem.alena.countries.data.local.CountryNoteEntity
import dem.alena.countries.testutil.FakeCountriesApi
import dem.alena.countries.testutil.FakeCountriesCacheDao
import dem.alena.countries.testutil.FakeCountriesSettingsRepository
import dem.alena.countries.testutil.FakeCollectionsDao
import dem.alena.countries.testutil.FakeCountryNotesDao
import dem.alena.countries.testutil.FakeFavouritesDao
import dem.alena.countries.testutil.FakeUserProfileDao
import dem.alena.countries.testutil.FakeViewHistoryDao
import dem.alena.countries.testutil.MainDispatcherRule
import dem.alena.countries.testutil.TEST_PROFILE_ID
import dem.alena.countries.testutil.testCountry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        api: FakeCountriesApi = FakeCountriesApi().apply { enqueueGetAllSuccess(listOf(testCountry())) }
    ): CountriesViewModel {
        val settings = FakeCountriesSettingsRepository()
        val profiles = ProfilesRepository(FakeUserProfileDao(), settings)
        val personal = PersonalDataRepository(
            FakeViewHistoryDao(),
            FakeCountryNotesDao(),
            FakeCollectionsDao()
        )
        val repository = CountriesRepository(api, FakeFavouritesDao(), FakeCountriesCacheDao(), settings)
        return CountriesViewModel(repository, settings, profiles, personal)
    }

    @Test
    fun initialState_isLoading() = runTest {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.content is CountriesContentState.Loading)
    }

    @Test
    fun initialLoad_success_setsSuccessState() = runTest {
        val api = FakeCountriesApi().apply { enqueueGetAllSuccess(listOf(testCountry(code = "USA"))) }
        val viewModel = createViewModel(api)
        advanceUntilIdle()

        val state = viewModel.uiState.value.content as CountriesContentState.Success
        assertEquals("USA", state.countries.first().country.code)
    }

    @Test
    fun refresh_afterError_reloadsCurrentQuery() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllError(IllegalStateException("fail"))
            enqueueGetAllSuccess(listOf(testCountry(code = "FRA")))
        }
        val viewModel = createViewModel(api)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.content is CountriesContentState.Error)

        viewModel.onRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value.content as CountriesContentState.Success
        assertEquals("FRA", state.countries.first().country.code)
    }

    @Test
    fun search_usesDebounce_beforeCallingApi() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllSuccess(listOf(testCountry(code = "USA")))
            setSearchSuccess("can", listOf(testCountry(code = "CAN")))
        }
        val viewModel = createViewModel(api)
        advanceUntilIdle()
        viewModel.onQueryChange("can")
        advanceTimeBy(399)
        assertEquals(0, api.searchCalls)
        advanceTimeBy(1)
        advanceUntilIdle()
        val state = viewModel.uiState.value.content as CountriesContentState.Success
        assertEquals(1, api.searchCalls)
        assertEquals("CAN", state.countries.first().country.code)
    }

    @Test
    fun withNotesFilter_showsOnlyCountriesWithNotes() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllSuccess(
                listOf(
                    testCountry(code = "A", name = "A"),
                    testCountry(code = "B", name = "B")
                )
            )
        }
        val notesDao = FakeCountryNotesDao()
        val viewModel = CountriesViewModel(
            CountriesRepository(api, FakeFavouritesDao(), FakeCountriesCacheDao(), FakeCountriesSettingsRepository()),
            FakeCountriesSettingsRepository(),
            ProfilesRepository(FakeUserProfileDao(), FakeCountriesSettingsRepository()),
            PersonalDataRepository(FakeViewHistoryDao(), notesDao, FakeCollectionsDao())
        )
        notesDao.upsert(
            CountryNoteEntity(TEST_PROFILE_ID, "B", "заметка", System.currentTimeMillis())
        )
        advanceUntilIdle()
        viewModel.onFilterModeChange(CountriesFilterMode.WITH_NOTES_ONLY)
        advanceUntilIdle()

        val state = viewModel.uiState.value.content as CountriesContentState.Success
        assertEquals(1, state.countries.size)
        assertEquals("B", state.countries.first().country.code)
    }

    @Test
    fun offlineFromCache_showsBanner() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllError(IllegalStateException("no network"))
        }
        val cache = FakeCountriesCacheDao().apply {
            seed(
                dem.alena.countries.data.repository.OfflineSyncPolicy.ALL_COUNTRIES_LIST_KEY,
                listOf(testCountry(code = "NLD")),
                System.currentTimeMillis()
            )
        }
        val settings = FakeCountriesSettingsRepository()
        val viewModel = CountriesViewModel(
            CountriesRepository(api, FakeFavouritesDao(), cache, settings),
            settings,
            ProfilesRepository(FakeUserProfileDao(), settings),
            PersonalDataRepository(FakeViewHistoryDao(), FakeCountryNotesDao(), FakeCollectionsDao())
        )
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.offlineBanner)
    }
}
