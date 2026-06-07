package dem.alena.countries.ui.screen.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.repository.PersonalDataRepository
import dem.alena.countries.data.repository.ProfilesRepository
import dem.alena.countries.data.repository.UserCollection
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CollectionsTab {
    MINE,
    SHARED
}

data class CollectionsUiState(
    val selectedTab: CollectionsTab = CollectionsTab.MINE,
    val myCollections: List<UserCollection> = emptyList(),
    val sharedCollections: List<UserCollection> = emptyList(),
    val newCollectionName: String = "",
    val activeProfileName: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val personalDataRepository: PersonalDataRepository,
    private val settingsRepository: CountriesSettingsRepository,
    private val profilesRepository: ProfilesRepository
) : ViewModel() {

    private val newName = MutableStateFlow("")
    private val selectedTab = MutableStateFlow(CollectionsTab.MINE)

    val listState: StateFlow<CollectionsUiState> = combine(
        settingsRepository.settings.flatMapLatest { settings ->
            combine(
                personalDataRepository.observeMyCollections(settings.activeProfileId),
                personalDataRepository.observeSharedCollections(settings.activeProfileId),
                profilesRepository.observeProfiles()
            ) { mine, shared, profiles ->
                val profileName = profiles.firstOrNull { it.id == settings.activeProfileId }?.name.orEmpty()
                Triple(mine, shared, profileName)
            }
        },
        newName,
        selectedTab
    ) { collectionsPack, draft, tab ->
        val (mine, shared, profileName) = collectionsPack
        CollectionsUiState(
            selectedTab = tab,
            myCollections = mine,
            sharedCollections = shared,
            newCollectionName = draft,
            activeProfileName = profileName
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CollectionsUiState())

    fun selectTab(tab: CollectionsTab) {
        selectedTab.value = tab
    }

    fun onNewNameChange(value: String) {
        newName.value = value
    }

    fun createCollection() {
        val name = listState.value.newCollectionName.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            val profileId = profilesRepository.getActiveProfileId()
            personalDataRepository.createCollection(profileId, name)
            newName.value = ""
            selectedTab.value = CollectionsTab.MINE
        }
    }

    fun deleteCollection(id: Long) {
        viewModelScope.launch {
            personalDataRepository.deleteCollection(id)
        }
    }
}
