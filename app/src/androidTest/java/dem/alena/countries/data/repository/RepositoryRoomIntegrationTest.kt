package dem.alena.countries.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dem.alena.countries.data.local.CountriesDatabase
import dem.alena.countries.testutil.AndroidFakeCountriesApi
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

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            CountriesDatabase::class.java
        ).build()
        repository = CountriesRepository(AndroidFakeCountriesApi(), database.favouritesDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun addFavourite_thenReadBack_persistsCountryData() = runBlocking {
        val country = androidTestCountry(code = "FRA", name = "France")

        repository.addFavourite(country)
        val loaded = repository.getFavouriteCountries()

        assertEquals(1, loaded.size)
        assertEquals("FRA", loaded.first().code)
        assertEquals("France", loaded.first().name.common)
    }

    @Test
    fun addFavouriteTwice_noDuplicatesInRoom() = runBlocking {
        val country = androidTestCountry(code = "USA")

        repository.addFavourite(country)
        repository.addFavourite(country)

        assertEquals(1, repository.getFavouriteCountries().size)
    }

    @Test
    fun removeFavourite_deletesFromRoom() = runBlocking {
        val country = androidTestCountry(code = "JPN")
        repository.addFavourite(country)

        repository.removeFavourite("JPN")

        assertTrue(repository.getFavouriteCountries().isEmpty())
    }
}

