package dem.alena.countries.data.repository

import dem.alena.countries.data.local.UserProfileDao
import dem.alena.countries.data.local.UserProfileEntity
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class UserProfile(
    val id: Long,
    val name: String,
    val hasPin: Boolean,
    val shareCollections: Boolean,
    val isActive: Boolean
)

sealed class ProfileSwitchResult {
    data object Success : ProfileSwitchResult()
    data object WrongPin : ProfileSwitchResult()
    data class Error(val message: String) : ProfileSwitchResult()
}

@Singleton
class ProfilesRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val settingsRepository: CountriesSettingsRepository
) {

    fun observeProfiles(): Flow<List<UserProfile>> {
        return kotlinx.coroutines.flow.combine(
            userProfileDao.observeAll(),
            settingsRepository.settings
        ) { entities, settings ->
            entities.map { entity ->
                UserProfile(
                    id = entity.id,
                    name = entity.name,
                    hasPin = entity.pinHash.isNotEmpty(),
                    shareCollections = entity.shareCollections,
                    isActive = entity.id == settings.activeProfileId
                )
            }
        }
    }

    suspend fun ensureDefaultProfile(): Long {
        val profiles = userProfileDao.getAll()
        if (profiles.isNotEmpty()) {
            val settings = settingsRepository.settings.first()
            if (settings.activeProfileId == 0L ||
                profiles.none { it.id == settings.activeProfileId }
            ) {
                settingsRepository.setActiveProfileId(profiles.first().id)
            }
            return settingsRepository.settings.first().activeProfileId
        }

        val id = userProfileDao.insert(
            UserProfileEntity(
                name = "Основной",
                pinHash = "",
                shareCollections = false,
                createdAt = System.currentTimeMillis()
            )
        )
        settingsRepository.setActiveProfileId(id)
        return id
    }

    suspend fun createProfile(name: String, pin: String = ""): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Имя профиля не может быть пустым" }
        return userProfileDao.insert(
            UserProfileEntity(
                name = trimmed,
                pinHash = ProfilePinHasher.hash(pin),
                shareCollections = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun switchProfile(profileId: Long, pin: String = ""): ProfileSwitchResult {
        val profile = userProfileDao.getById(profileId)
            ?: return ProfileSwitchResult.Error("Профиль не найден")
        if (profile.pinHash.isNotEmpty() && !ProfilePinHasher.matches(pin, profile.pinHash)) {
            return ProfileSwitchResult.WrongPin
        }
        settingsRepository.setActiveProfileId(profile.id)
        return ProfileSwitchResult.Success
    }

    suspend fun setShareCollections(profileId: Long, enabled: Boolean) {
        userProfileDao.updateShareCollections(profileId, enabled)
    }

    suspend fun updatePin(profileId: Long, newPin: String) {
        userProfileDao.updatePin(profileId, ProfilePinHasher.hash(newPin))
    }

    suspend fun deleteProfile(profileId: Long) {
        val profiles = userProfileDao.getAll()
        if (profiles.size <= 1) {
            throw IllegalStateException("Нельзя удалить единственный профиль")
        }
        userProfileDao.deleteById(profileId)
        val active = settingsRepository.settings.first().activeProfileId
        if (active == profileId) {
            val next = userProfileDao.getAll().firstOrNull()
            if (next != null) {
                settingsRepository.setActiveProfileId(next.id)
            }
        }
    }

    suspend fun getActiveProfileId(): Long {
        ensureDefaultProfile()
        return settingsRepository.settings.first().activeProfileId
    }

    suspend fun getActiveProfile(): UserProfile? {
        val id = getActiveProfileId()
        val entity = userProfileDao.getById(id) ?: return null
        return UserProfile(
            id = entity.id,
            name = entity.name,
            hasPin = entity.pinHash.isNotEmpty(),
            shareCollections = entity.shareCollections,
            isActive = true
        )
    }
}
