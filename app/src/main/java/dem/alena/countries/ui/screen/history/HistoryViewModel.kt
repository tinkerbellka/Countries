package dem.alena.countries.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.repository.HistoryItem
import dem.alena.countries.data.repository.PersonalDataRepository
import dem.alena.countries.data.repository.ProfilesRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val isEmpty: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val personalDataRepository: PersonalDataRepository,
    private val settingsRepository: CountriesSettingsRepository,
    private val profilesRepository: ProfilesRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = settingsRepository.settings
        .flatMapLatest { settings ->
            personalDataRepository.observeRecentHistory(settings.activeProfileId)
        }
        .map { items -> HistoryUiState(items = items, isEmpty = items.isEmpty()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HistoryUiState())

    init {
        viewModelScope.launch { profilesRepository.ensureDefaultProfile() }
    }

    fun clearHistory() {
        viewModelScope.launch {
            val profileId = profilesRepository.getActiveProfileId()
            personalDataRepository.clearHistory(profileId)
        }
    }
}
