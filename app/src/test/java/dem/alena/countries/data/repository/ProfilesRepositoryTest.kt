package dem.alena.countries.data.repository

import dem.alena.countries.data.local.UserProfileEntity
import dem.alena.countries.testutil.FakeCountriesSettingsRepository
import dem.alena.countries.testutil.FakeUserProfileDao
import dem.alena.countries.testutil.TEST_PROFILE_ID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfilesRepositoryTest {

    @Test
    fun switchProfile_wrongPin_rejected() = runTest {
        val dao = FakeUserProfileDao()
        dao.insert(
            UserProfileEntity(
                name = "Секретный",
                pinHash = ProfilePinHasher.hash("1234"),
                shareCollections = false,
                createdAt = 0L
            )
        )
        val settings = FakeCountriesSettingsRepository()
        val repository = ProfilesRepository(dao, settings)

        val result = repository.switchProfile(2L, "0000")
        assertTrue(result is ProfileSwitchResult.WrongPin)
    }

    @Test
    fun switchProfile_correctPin_succeeds() = runTest {
        val settings = FakeCountriesSettingsRepository()
        val repository = ProfilesRepository(FakeUserProfileDao(), settings)

        val result = repository.switchProfile(TEST_PROFILE_ID, "")
        assertTrue(result is ProfileSwitchResult.Success)
    }
}
