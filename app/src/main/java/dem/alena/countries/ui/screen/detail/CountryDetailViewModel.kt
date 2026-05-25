package dem.alena.countries.ui.screen.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.data.repository.PersonalDataRepository
import dem.alena.countries.data.repository.ProfilesRepository
import dem.alena.countries.data.repository.UserCollection
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CountryDetailUiState(
    val country: Country? = null,
    val isLoading: Boolean = false,
    val error: Boolean = false,
    val errorMessage: String? = null,
    val isFavourite: Boolean = false,
    val noteText: String = "",
    val collections: List<UserCollection> = emptyList(),
    val selectedCollectionId: Long? = null
)

@HiltViewModel
class CountryDetailViewModel @Inject constructor(
    private val repository: CountriesRepository,
    private val personalDataRepository: PersonalDataRepository,
    private val profilesRepository: ProfilesRepository
) : ViewModel() {

    var uiState by mutableStateOf(CountryDetailUiState())
        private set

    private var lastLoadedCode: String? = null
    private var activeProfileId: Long = 0L

    fun load(code: String) {
        lastLoadedCode = code
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = false, errorMessage = null)
            try {
                activeProfileId = profilesRepository.getActiveProfileId()
                val country = repository.getCountryByCode(code, activeProfileId)
                val isFav = repository.isFavourite(activeProfileId, code)
                val note = personalDataRepository.observeNote(activeProfileId, code).first()
                val collections = personalDataRepository
                    .observeMyCollections(activeProfileId)
                    .first()

                personalDataRepository.recordView(activeProfileId, country)

                uiState = uiState.copy(
                    country = country,
                    isFavourite = isFav,
                    noteText = note,
                    collections = collections,
                    isLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    country = null,
                    error = true,
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun retry() {
        val code = lastLoadedCode ?: return
        load(code)
    }

    fun onNoteChange(text: String) {
        uiState = uiState.copy(noteText = text)
    }

    fun saveNote() {
        val code = lastLoadedCode ?: return
        viewModelScope.launch {
            personalDataRepository.saveNote(activeProfileId, code, uiState.noteText)
        }
    }

    fun toggleFavourite() {
        val country = uiState.country ?: return
        viewModelScope.launch {
            if (uiState.isFavourite) {
                repository.removeFavourite(activeProfileId, country.code)
            } else {
                repository.addFavourite(activeProfileId, country)
            }
            uiState = uiState.copy(isFavourite = !uiState.isFavourite)
        }
    }

    fun onCollectionSelected(collectionId: Long) {
        uiState = uiState.copy(selectedCollectionId = collectionId)
    }

    fun addToSelectedCollection() {
        val country = uiState.country ?: return
        val collectionId = uiState.selectedCollectionId ?: return
        viewModelScope.launch {
            personalDataRepository.addCountryToCollection(collectionId, country)
        }
    }
}
