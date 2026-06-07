package dem.alena.countries.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dem.alena.countries.data.preferences.AppThemeMode
import dem.alena.countries.data.preferences.CountriesListSettings
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.repository.ProfileSwitchResult
import dem.alena.countries.data.repository.ProfilesRepository
import dem.alena.countries.data.repository.UserProfile
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: CountriesListSettings = CountriesListSettings(),
    val profiles: List<UserProfile> = emptyList(),
    val newProfileName: String = "",
    val newProfilePin: String = "",
    val pinDialogProfileId: Long? = null,
    val pinDialogProfileName: String = "",
    val enteredPin: String = "",
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: CountriesSettingsRepository,
    private val profilesRepository: ProfilesRepository
) : ViewModel() {

    private val draft = MutableStateFlow(ProfileDraft())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        profilesRepository.observeProfiles(),
        draft
    ) { settings, profiles, form ->
        SettingsUiState(
            settings = settings,
            profiles = profiles,
            newProfileName = form.newProfileName,
            newProfilePin = form.newProfilePin,
            pinDialogProfileId = form.pinDialogProfileId,
            pinDialogProfileName = form.pinDialogProfileName,
            enteredPin = form.enteredPin,
            message = form.message
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun onNewProfileNameChange(value: String) {
        draft.update { it.copy(newProfileName = value, message = null) }
    }

    fun onNewProfilePinChange(value: String) {
        draft.update { it.copy(newProfilePin = value.filter(Char::isDigit).take(6), message = null) }
    }

    fun setThemeMode(value: AppThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(value) }
    }

    fun onShareCollectionsChange(enabled: Boolean) {
        viewModelScope.launch {
            val profileId = profilesRepository.getActiveProfileId()
            profilesRepository.setShareCollections(profileId, enabled)
        }
    }

    fun requestSwitchProfile(profile: UserProfile) {
        if (profile.isActive) return
        if (profile.hasPin) {
            draft.update {
                it.copy(
                    pinDialogProfileId = profile.id,
                    pinDialogProfileName = profile.name,
                    enteredPin = "",
                    message = null
                )
            }
        } else {
            viewModelScope.launch { performSwitch(profile.id, "") }
        }
    }

    fun onEnteredPinChange(value: String) {
        draft.update { it.copy(enteredPin = value.filter(Char::isDigit).take(6)) }
    }

    fun confirmPinDialog() {
        val profileId = draft.value.pinDialogProfileId ?: return
        val pin = draft.value.enteredPin
        viewModelScope.launch { performSwitch(profileId, pin) }
    }

    fun dismissPinDialog() {
        draft.update {
            it.copy(pinDialogProfileId = null, pinDialogProfileName = "", enteredPin = "")
        }
    }

    fun createProfile() {
        val name = draft.value.newProfileName.trim()
        if (name.isEmpty()) {
            draft.update { it.copy(message = "Введите имя профиля") }
            return
        }
        viewModelScope.launch {
            val id = profilesRepository.createProfile(name, draft.value.newProfilePin)
            when (profilesRepository.switchProfile(id, draft.value.newProfilePin)) {
                ProfileSwitchResult.Success -> {
                    draft.update {
                        it.copy(
                            newProfileName = "",
                            newProfilePin = "",
                            message = "Профиль «$name» создан"
                        )
                    }
                }
                else -> draft.update { it.copy(message = "Не удалось войти в новый профиль") }
            }
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            try {
                profilesRepository.deleteProfile(profileId)
                draft.update { it.copy(message = null) }
            } catch (e: Exception) {
                draft.update { it.copy(message = e.message) }
            }
        }
    }

    fun clearMessage() {
        draft.update { it.copy(message = null) }
    }

    private suspend fun performSwitch(profileId: Long, pin: String) {
        when (val result = profilesRepository.switchProfile(profileId, pin)) {
            ProfileSwitchResult.Success -> {
                draft.update {
                    it.copy(
                        pinDialogProfileId = null,
                        pinDialogProfileName = "",
                        enteredPin = "",
                        message = null
                    )
                }
            }
            ProfileSwitchResult.WrongPin -> {
                draft.update { it.copy(message = "Неверный PIN") }
            }
            is ProfileSwitchResult.Error -> {
                draft.update { it.copy(message = result.message) }
            }
        }
    }

    private data class ProfileDraft(
        val newProfileName: String = "",
        val newProfilePin: String = "",
        val pinDialogProfileId: Long? = null,
        val pinDialogProfileName: String = "",
        val enteredPin: String = "",
        val message: String? = null
    )
}
