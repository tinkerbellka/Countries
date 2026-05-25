package dem.alena.countries.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dem.alena.countries.data.local.CountriesDatabase
import dem.alena.countries.data.local.FavouriteCountryEntity
import dem.alena.countries.data.local.UserProfileEntity
import dem.alena.countries.testutil.AndroidFakeCountriesApi
import kotlinx.coroutines.flow.first
import dem.alena.countries.testutil.AndroidFakeCountriesSettingsRepository
import dem.alena.countries.testutil.androidTestCountry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryRoomIntegrationTest {

    private lateinit var database: CountriesDatabase
    private lateinit var repository: CountriesRepository
    private lateinit var personalData: PersonalDataRepository
    private var profileId: Long = 0L

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CountriesDatabase::class.java).build()
        repository = CountriesRepository(
            AndroidFakeCountriesApi(),
            database.favouritesDao(),
            database.countriesCacheDao(),
            AndroidFakeCountriesSettingsRepository()
        )
        personalData = PersonalDataRepository(
            database.viewHistoryDao(),
            database.countryNotesDao(),
            database.collectionsDao()
        )
        runBlocking {
            profileId = database.userProfileDao().insert(
                UserProfileEntity(name = "Test", createdAt = System.currentTimeMillis())
            )
        }
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun addFavourite_thenReadBack_persistsCountryData() = runBlocking {
        val country = androidTestCountry(code = "FRA", name = "France")
        repository.addFavourite(profileId, country)
        val loaded = repository.getFavouriteCountries(profileId)
        assertEquals(1, loaded.size)
        assertEquals("FRA", loaded.first().code)
    }

    @Test
    fun addFavouriteTwice_noDuplicatesInRoom() = runBlocking {
        val country = androidTestCountry(code = "USA")
        repository.addFavourite(profileId, country)
        repository.addFavourite(profileId, country)
        assertEquals(1, repository.getFavouriteCountries(profileId).size)
    }

    @Test
    fun viewHistory_persistsAfterInsert() = runBlocking {
        val country = androidTestCountry(code = "JPN", name = "Japan")
        personalData.recordView(profileId, country)
        val items = personalData.observeRecentHistory(profileId).first()
        assertEquals(1, items.size)
        assertEquals("JPN", items.first().countryCode)
    }

    @Test
    fun cache_persistsCountriesForOffline() = runBlocking {
        val api = AndroidFakeCountriesApi().apply {
            enqueueGetAllSuccess(listOf(androidTestCountry(code = "DEU")))
        }
        val repo = CountriesRepository(
            api,
            database.favouritesDao(),
            database.countriesCacheDao(),
            AndroidFakeCountriesSettingsRepository()
        )
        repo.fetchCountries("", ttlHours = 24)
        val cached = database.countriesCacheDao().getByListKey(OfflineSyncPolicy.ALL_COUNTRIES_LIST_KEY)
        assertTrue(cached.isNotEmpty())
    }
}
