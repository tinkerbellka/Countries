package dem.alena.countries.ui.screen.list

import dem.alena.countries.data.preferences.CountriesSortOrder
import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.testutil.FakeCountriesApi
import dem.alena.countries.testutil.FakeCountriesSettingsRepository
import dem.alena.countries.testutil.FakeFavouritesDao
import dem.alena.countries.testutil.MainDispatcherRule
import dem.alena.countries.testutil.testCountry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_isLoading() = runTest {
        val api = FakeCountriesApi().apply { enqueueGetAllSuccess(listOf(testCountry())) }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository, FakeCountriesSettingsRepository())

        assertTrue(viewModel.uiState.value.content is CountriesContentState.Loading)
    }

    @Test
    fun initialLoad_success_setsSuccessState() = runTest {
        val api = FakeCountriesApi().apply { enqueueGetAllSuccess(listOf(testCountry(code = "USA"))) }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository, FakeCountriesSettingsRepository())

        advanceUntilIdle()

        val state = viewModel.uiState.value.content as CountriesContentState.Success
        assertEquals(1, state.countries.size)
        assertEquals("USA", state.countries.first().country.code)
    }

    @Test
    fun refresh_afterError_reloadsCurrentQuery() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllError(IllegalStateException("first failure"))
            enqueueGetAllSuccess(listOf(testCountry(code = "FRA")))
        }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository, FakeCountriesSettingsRepository())

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.content is CountriesContentState.Error)

        viewModel.onRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value.content as CountriesContentState.Success
        assertEquals("FRA", state.countries.first().country.code)
        assertEquals(2, api.getAllCalls)
    }

    @Test
    fun search_usesDebounce_beforeCallingApi() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllSuccess(listOf(testCountry(code = "USA", name = "United States")))
            setSearchSuccess("can", listOf(testCountry(code = "CAN", name = "Canada")))
        }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository, FakeCountriesSettingsRepository())

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
    fun toggleFavourite_updatesVisibleItemsFromRoomFlow() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllSuccess(listOf(testCountry(code = "JPN", name = "Japan")))
        }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository, FakeCountriesSettingsRepository())

        advanceUntilIdle()
        viewModel.toggleFavourite(testCountry(code = "JPN", name = "Japan"))
        advanceUntilIdle()

        val state = viewModel.uiState.value.content as CountriesContentState.Success
        assertTrue(state.countries.first().isFavourite)
        assertEquals(1, viewModel.uiState.value.favouritesCount)
    }

    @Test
    fun favouritesOnly_filter_reactsToRoomFlow() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllSuccess(
                listOf(
                    testCountry(code = "JPN", name = "Japan"),
                    testCountry(code = "BRA", name = "Brazil")
                )
            )
        }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository, FakeCountriesSettingsRepository())

        advanceUntilIdle()
        viewModel.toggleFavourite(testCountry(code = "BRA", name = "Brazil"))
        advanceUntilIdle()
        viewModel.onFilterModeChange(CountriesFilterMode.FAVOURITES_ONLY)
        advanceUntilIdle()

        val state = viewModel.uiState.value.content as CountriesContentState.Success
        assertEquals(1, state.countries.size)
        assertEquals("BRA", state.countries.first().country.code)
    }

    @Test
    fun sortOrder_change_reordersList_withoutReloadingApi() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllSuccess(
                listOf(
                    testCountry(code = "SML", name = "Small").copy(population = 10),
                    testCountry(code = "BIG", name = "Big").copy(population = 1000)
                )
            )
        }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val settingsRepository = FakeCountriesSettingsRepository()
        val viewModel = CountriesViewModel(repository, settingsRepository)

        advanceUntilIdle()
        viewModel.onSortOrderChange(CountriesSortOrder.POPULATION_DESC)
        advanceUntilIdle()

        val state = viewModel.uiState.value.content as CountriesContentState.Success
        assertEquals("BIG", state.countries.first().country.code)
        assertEquals(1, api.getAllCalls)
    }
}
