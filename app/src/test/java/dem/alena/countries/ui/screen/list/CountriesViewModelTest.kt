package dem.alena.countries.ui.screen.list

import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.testutil.FakeCountriesApi
import dem.alena.countries.testutil.FakeFavouritesDao
import dem.alena.countries.testutil.MainDispatcherRule
import dem.alena.countries.testutil.testCountry
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

        val viewModel = CountriesViewModel(repository)

        assertTrue(viewModel.uiState is CountriesUiState.Loading)
    }

    @Test
    fun initialLoad_success_setsSuccessState() = runTest {
        val api = FakeCountriesApi().apply { enqueueGetAllSuccess(listOf(testCountry(code = "USA"))) }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState as CountriesUiState.Success
        assertEquals(1, state.countries.size)
        assertEquals("USA", state.countries.first().code)
    }

    @Test
    fun initialLoad_error_setsErrorState() = runTest {
        val api = FakeCountriesApi().apply { enqueueGetAllError(IllegalStateException("boom")) }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository)

        advanceUntilIdle()

        assertTrue(viewModel.uiState is CountriesUiState.Error)
    }

    @Test
    fun retry_afterError_startsNewRequest_andBecomesSuccess() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllError(IllegalStateException("first failure"))
            enqueueGetAllSuccess(listOf(testCountry(code = "FRA")))
        }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository)

        advanceUntilIdle()
        assertTrue(viewModel.uiState is CountriesUiState.Error)

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState as CountriesUiState.Success
        assertEquals("FRA", state.countries.first().code)
        assertEquals(2, api.getAllCalls)
    }

    @Test
    fun retry_afterError_setsLoadingImmediately_beforeSuccess() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllError(IllegalStateException("first failure"))
            enqueueGetAllSuccess(listOf(testCountry(code = "ESP")))
        }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository)
        advanceUntilIdle()

        viewModel.retry()
        assertTrue(viewModel.uiState is CountriesUiState.Loading)
        advanceUntilIdle()

        val state = viewModel.uiState as CountriesUiState.Success
        assertEquals("ESP", state.countries.first().code)
    }

    @Test
    fun search_emptyResult_setsEmpty_notSuccessWithEmptyList() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllSuccess(listOf(testCountry()))
            setSearchSuccess("zzz", emptyList())
        }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository)
        advanceUntilIdle()

        viewModel.search("zzz")
        advanceUntilIdle()

        assertTrue(viewModel.uiState is CountriesUiState.Empty)
    }

    @Test
    fun loadFavourites_readsFromRoom_andShowsSuccess() = runTest {
        val api = FakeCountriesApi().apply { enqueueGetAllSuccess(listOf(testCountry())) }
        val dao = FakeFavouritesDao()
        val repository = CountriesRepository(api, dao)
        val viewModel = CountriesViewModel(repository)
        advanceUntilIdle()

        viewModel.toggleFavourite(testCountry(code = "JPN", name = "Japan"))
        advanceUntilIdle()
        viewModel.onFavouritesEvent(FavouritesEvent.Load)
        advanceUntilIdle()

        val state = viewModel.favouritesState as FavouritesUiState.Success
        assertEquals(1, state.countries.size)
        assertEquals("JPN", state.countries.first().code)
    }

    @Test
    fun search_blankQuery_reloadsAllCountries() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllSuccess(listOf(testCountry(code = "USA")))
            enqueueGetAllSuccess(listOf(testCountry(code = "ITA")))
        }
        val repository = CountriesRepository(api, FakeFavouritesDao())
        val viewModel = CountriesViewModel(repository)
        advanceUntilIdle()

        viewModel.search("")
        advanceUntilIdle()

        val state = viewModel.uiState as CountriesUiState.Success
        assertEquals("ITA", state.countries.first().code)
        assertEquals(2, api.getAllCalls)
    }
}

