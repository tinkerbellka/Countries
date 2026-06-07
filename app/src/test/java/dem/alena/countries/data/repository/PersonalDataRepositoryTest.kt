package dem.alena.countries.data.repository

import dem.alena.countries.testutil.FakeCollectionsDao
import dem.alena.countries.testutil.FakeCountryNotesDao
import dem.alena.countries.testutil.FakeViewHistoryDao
import dem.alena.countries.testutil.TEST_PROFILE_ID
import dem.alena.countries.testutil.testCountry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalDataRepositoryTest {

    private val repository = PersonalDataRepository(
        FakeViewHistoryDao(),
        FakeCountryNotesDao(),
        FakeCollectionsDao()
    )

    @Test
    fun recordView_addsHistoryEntry() = runTest {
        repository.recordView(TEST_PROFILE_ID, testCountry(code = "ESP", name = "Spain"))
        val items = repository.observeRecentHistory(TEST_PROFILE_ID).first()
        assertEquals(1, items.size)
        assertEquals("ESP", items.first().countryCode)
    }

    @Test
    fun saveNote_andFilterByNoteCodes() = runTest {
        repository.saveNote(TEST_PROFILE_ID, "DEU", "столица Берлин")
        val codes = repository.observeNoteCodes(TEST_PROFILE_ID).first()
        assertTrue(codes.contains("DEU"))
    }

    @Test
    fun createCollection_andAddCountry() = runTest {
        val collectionId = repository.createCollection(TEST_PROFILE_ID, "Путешествия")
        repository.addCountryToCollection(collectionId, testCountry(code = "NOR", name = "Norway"))
        val countries = repository.observeCollectionCountries(collectionId).first()
        assertEquals("NOR", countries.first().countryCode)
    }
}
