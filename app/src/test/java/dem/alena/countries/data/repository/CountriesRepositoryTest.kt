package dem.alena.countries.data.repository

import dem.alena.countries.data.repository.OfflineSyncPolicy.ALL_COUNTRIES_LIST_KEY
import dem.alena.countries.testutil.FakeCountriesApi
import dem.alena.countries.testutil.FakeCountriesCacheDao
import dem.alena.countries.testutil.FakeCountriesSettingsRepository
import dem.alena.countries.testutil.FakeFavouritesDao
import dem.alena.countries.testutil.TEST_PROFILE_ID
import dem.alena.countries.testutil.countryToCacheEntity
import dem.alena.countries.testutil.testCountry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountriesRepositoryTest {

    @Test
    fun fetchCountries_networkSuccess_savesCache() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllSuccess(listOf(testCountry(code = "DEU")))
        }
        val cache = FakeCountriesCacheDao()
        val repository = CountriesRepository(api, FakeFavouritesDao(), cache, FakeCountriesSettingsRepository())

        val result = repository.fetchCountries("", ttlHours = 24)

        assertEquals(DataSource.NETWORK, result.source)
        assertEquals(1, result.countries.size)
        assertTrue(cache.getByListKey(ALL_COUNTRIES_LIST_KEY).isNotEmpty())
    }

    @Test
    fun fetchCountries_networkFails_returnsStaleCache() = runTest {
        val api = FakeCountriesApi().apply {
            enqueueGetAllError(IllegalStateException("offline"))
        }
        val cache = FakeCountriesCacheDao().apply {
            val staleTime = System.currentTimeMillis() - 48 * 60 * 60 * 1000
            seed(ALL_COUNTRIES_LIST_KEY, listOf(testCountry(code = "JPN")), staleTime)
        }
        val repository = CountriesRepository(api, FakeFavouritesDao(), cache, FakeCountriesSettingsRepository())

        val result = repository.fetchCountries("", ttlHours = 24)

        assertEquals(DataSource.CACHE, result.source)
        assertTrue(result.isStale)
        assertEquals("JPN", result.countries.first().code)
    }

    @Test
    fun getCountryByCode_offline_usesCache() = runTest {
        val api = FakeCountriesApi()
        val cache = FakeCountriesCacheDao().apply {
            seed(
                OfflineSyncPolicy.ALL_COUNTRIES_LIST_KEY,
                listOf(testCountry(code = "FIN", name = "Finland")),
                System.currentTimeMillis()
            )
        }
        val repository = CountriesRepository(api, FakeFavouritesDao(), cache, FakeCountriesSettingsRepository())

        val country = repository.getCountryByCode("FIN", TEST_PROFILE_ID)

        assertEquals("FIN", country.code)
        assertEquals("Finland", country.name.common)
    }

    @Test
    fun addFavourite_scopedByProfile() = runTest {
        val dao = FakeFavouritesDao()
        val repository = CountriesRepository(
            FakeCountriesApi(),
            dao,
            FakeCountriesCacheDao(),
            FakeCountriesSettingsRepository()
        )

        repository.addFavourite(TEST_PROFILE_ID, testCountry(code = "FRA"))
        assertTrue(repository.isFavourite(TEST_PROFILE_ID, "FRA"))
        assertTrue(!repository.isFavourite(999L, "FRA"))
    }
}
