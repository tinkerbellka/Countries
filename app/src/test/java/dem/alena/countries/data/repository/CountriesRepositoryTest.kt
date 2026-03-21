package dem.alena.countries.data.repository

import dem.alena.countries.testutil.FakeCountriesApi
import dem.alena.countries.testutil.FakeFavouritesDao
import dem.alena.countries.testutil.testCountry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountriesRepositoryTest {

    @Test
    fun addFavourite_sameCountryTwice_doesNotCreateDuplicates() = runTest {
        val repository = CountriesRepository(FakeCountriesApi(), FakeFavouritesDao())
        val country = testCountry(code = "USA")

        repository.addFavourite(country)
        repository.addFavourite(country)

        val favourites = repository.getFavouriteCountries()
        assertEquals(1, favourites.size)
        assertEquals("USA", favourites.first().code)
    }

    @Test
    fun getFavouriteCountries_mapsRoomEntityToCountryCorrectly() = runTest {
        val repository = CountriesRepository(FakeCountriesApi(), FakeFavouritesDao())
        val country = testCountry(code = "DEU", name = "Germany")

        repository.addFavourite(country)

        val mapped = repository.getFavouriteCountries().first()
        assertEquals("DEU", mapped.code)
        assertEquals("Germany", mapped.name.common)
        assertTrue(mapped.capital?.isNotEmpty() == true)
    }
}

