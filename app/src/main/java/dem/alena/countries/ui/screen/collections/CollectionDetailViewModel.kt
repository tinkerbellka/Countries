package dem.alena.countries.ui.screen.collections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dem.alena.countries.data.repository.PersonalDataRepository
import dem.alena.countries.data.repository.ProfilesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionDetailUiState(
    val collectionName: String = "",
    val ownerLabel: String = "",
    val countries: List<dem.alena.countries.data.repository.CollectionCountryItem> = emptyList(),
    val canEdit: Boolean = false
)

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val personalDataRepository: PersonalDataRepository,
    private val profilesRepository: ProfilesRepository
) : ViewModel() {

    private val collectionId: Long =
        savedStateHandle.get<String>("collectionId")?.toLongOrNull() ?: 0L

    private val collectionName = MutableStateFlow("")
    private val ownerLabel = MutableStateFlow("")
    private val canEdit = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val activeId = profilesRepository.getActiveProfileId()
            val owned = personalDataRepository.isCollectionOwnedBy(collectionId, activeId)
            canEdit.value = owned
            collectionName.value =
                personalDataRepository.getCollectionName(collectionId).orEmpty()
            ownerLabel.value = if (owned) "Моя коллекция" else "Коллекция другого профиля"
        }
    }

    val uiState: StateFlow<CollectionDetailUiState> = combine(
        personalDataRepository.observeCollectionCountries(collectionId),
        collectionName,
        ownerLabel,
        canEdit
    ) { countries, name, owner, editable ->
        CollectionDetailUiState(
            collectionName = name,
            ownerLabel = owner,
            countries = countries,
            canEdit = editable
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CollectionDetailUiState())

    fun removeCountry(countryCode: String) {
        if (!uiState.value.canEdit) return
        viewModelScope.launch {
            personalDataRepository.removeCountryFromCollection(collectionId, countryCode)
        }
    }
}
