package dem.alena.countries.ui.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.preferences.CountriesSortOrder
import dem.alena.countries.data.repository.CountriesFetchResult
import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.data.repository.PersonalDataRepository
import dem.alena.countries.data.repository.ProfilesRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val repository: CountriesRepository,
    private val settingsRepository: CountriesSettingsRepository,
    private val profilesRepository: ProfilesRepository,
    private val personalDataRepository: PersonalDataRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filterMode = MutableStateFlow(CountriesFilterMode.ALL)
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            profilesRepository.ensureDefaultProfile()
        }
    }

    private val favouriteCodes = settingsRepository.settings
        .flatMapLatest { settings ->
            if (settings.activeProfileId == 0L) {
                flowOf(emptySet())
            } else {
                repository.observeFavouriteCodes(settings.activeProfileId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val noteCodes = settingsRepository.settings
        .flatMapLatest { settings ->
            if (settings.activeProfileId == 0L) {
                flowOf(emptySet())
            } else {
                personalDataRepository.observeNoteCodes(settings.activeProfileId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val requestedQuery = merge(
        flowOf(""),
        query.drop(1).map(String::trim).debounce(SEARCH_DEBOUNCE_MILLIS)
    ).distinctUntilChanged()

    private val remoteCountries = combine(
        requestedQuery,
        refreshRequests.onStart { emit(Unit) },
        settingsRepository.settings
    ) { currentQuery, _, settings ->
        currentQuery to settings.cacheTtlHours
    }
        .flatMapLatest { (currentQuery, ttlHours) -> loadCountries(currentQuery, ttlHours) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RemoteCountriesState.Loading)

    private val listInputs = combine(
        combine(query, filterMode, settingsRepository.settings) { rawQuery, selectedFilter, settings ->
            Triple(rawQuery, selectedFilter, settings)
        },
        combine(
            profilesRepository.observeProfiles(),
            favouriteCodes,
            noteCodes
        ) { profiles, codes, notes ->
            Triple(profiles, codes, notes)
        }
    ) { left, right ->
        val (rawQuery, selectedFilter, settings) = left
        val (profiles, codes, notes) = right
        val profileName = profiles.firstOrNull { it.id == settings.activeProfileId }?.name.orEmpty()
        ListInputs(
            query = rawQuery,
            filterMode = selectedFilter,
            settings = settings,
            profileName = profileName,
            favouriteCodes = codes,
            noteCodes = notes
        )
    }

    val uiState = combine(listInputs, remoteCountries) { inputs, remoteState ->
        CountriesUiState(
            query = inputs.query,
            filterMode = inputs.filterMode,
            sortOrder = inputs.settings.sortOrder,
            favouritesCount = inputs.favouriteCodes.size,
            activeProfileName = inputs.profileName,
            offlineBanner = when (remoteState) {
                is RemoteCountriesState.Success -> remoteState.banner
                else -> null
            },
            content = remoteState.toContentState(
                favouriteCodes = inputs.favouriteCodes,
                noteCodes = inputs.noteCodes,
                filterMode = inputs.filterMode,
                sortOrder = inputs.settings.sortOrder
            )
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CountriesUiState())

    val favouritesUiState = combine(
        settingsRepository.settings.flatMapLatest { settings ->
            if (settings.activeProfileId == 0L) {
                flowOf(emptyList())
            } else {
                repository.observeFavouriteCountries(settings.activeProfileId)
            }
        },
        settingsRepository.settings
    ) { countries, settings ->
        val sortedCountries = sortCountries(countries, settings.sortOrder)
            .map { country -> CountryListItem(country = country, isFavourite = true) }

        if (sortedCountries.isEmpty()) FavouritesUiState.Empty
        else FavouritesUiState.Success(sortedCountries)
    }
        .onStart { emit(FavouritesUiState.Loading) }
        .catch { error ->
            emit(FavouritesUiState.Error("Не удалось загрузить избранное: ${error.message.orEmpty()}"))
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, FavouritesUiState.Loading)

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterModeChange(value: CountriesFilterMode) {
        filterMode.value = value
    }

    fun onSortOrderChange(value: CountriesSortOrder) {
        if (uiState.value.sortOrder == value) return
        viewModelScope.launch { settingsRepository.setSortOrder(value) }
    }

    fun onRefresh() {
        refreshRequests.tryEmit(Unit)
    }

    fun retry() {
        onRefresh()
    }

    fun toggleFavourite(country: Country) {
        viewModelScope.launch {
            val profileId = profilesRepository.getActiveProfileId()
            if (favouriteCodes.value.contains(country.code)) {
                repository.removeFavourite(profileId, country.code)
            } else {
                repository.addFavourite(profileId, country)
            }
        }
    }

    private fun loadCountries(currentQuery: String, ttlHours: Int): Flow<RemoteCountriesState> = flow {
        emit(RemoteCountriesState.Loading)
        val result = repository.fetchCountries(currentQuery, ttlHours)
        emit(
            RemoteCountriesState.Success(
                countries = result.countries,
                banner = result.source.toBannerText(result.isStale)
            )
        )
    }.catch { error ->
        if (error is HttpException && error.code() == 404) {
            emit(RemoteCountriesState.Success(emptyList(), banner = null))
        } else {
            emit(RemoteCountriesState.Error(error.toUserMessage()))
        }
    }

    private fun RemoteCountriesState.toContentState(
        favouriteCodes: Set<String>,
        noteCodes: Set<String>,
        filterMode: CountriesFilterMode,
        sortOrder: CountriesSortOrder
    ): CountriesContentState {
        return when (this) {
            RemoteCountriesState.Loading -> CountriesContentState.Loading
            is RemoteCountriesState.Error -> CountriesContentState.Error(message)
            is RemoteCountriesState.Success -> {
                val visibleCountries = countries
                    .map { country ->
                        CountryListItem(
                            country = country,
                            isFavourite = favouriteCodes.contains(country.code),
                            hasNote = noteCodes.contains(country.code)
                        )
                    }
                    .filter { item ->
                        when (filterMode) {
                            CountriesFilterMode.ALL -> true
                            CountriesFilterMode.FAVOURITES_ONLY -> item.isFavourite
                            CountriesFilterMode.WITH_NOTES_ONLY -> item.hasNote
                        }
                    }
                    .let { items ->
                        when (sortOrder) {
                            CountriesSortOrder.NAME ->
                                items.sortedBy { it.country.name.common.lowercase() }
                            CountriesSortOrder.POPULATION_DESC ->
                                items.sortedByDescending { it.country.population }
                        }
                    }

                if (visibleCountries.isEmpty()) CountriesContentState.Empty
                else CountriesContentState.Success(visibleCountries)
            }
        }
    }

    private fun sortCountries(countries: List<Country>, sortOrder: CountriesSortOrder): List<Country> {
        return when (sortOrder) {
            CountriesSortOrder.NAME -> countries.sortedBy { it.name.common.lowercase() }
            CountriesSortOrder.POPULATION_DESC -> countries.sortedByDescending { it.population }
        }
    }

    private fun Throwable.toUserMessage(): String {
        return "Не удалось загрузить страны: ${message.orEmpty()}"
    }

    private sealed interface RemoteCountriesState {
        data object Loading : RemoteCountriesState
        data class Success(val countries: List<Country>, val banner: String?) : RemoteCountriesState
        data class Error(val message: String) : RemoteCountriesState
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
    }

    private data class ListInputs(
        val query: String,
        val filterMode: CountriesFilterMode,
        val settings: dem.alena.countries.data.preferences.CountriesListSettings,
        val profileName: String,
        val favouriteCodes: Set<String>,
        val noteCodes: Set<String>
    )
}
