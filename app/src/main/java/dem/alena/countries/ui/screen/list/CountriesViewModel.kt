package dem.alena.countries.ui.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.preferences.CountriesSortOrder
import dem.alena.countries.data.repository.CountriesRepository
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
    private val settingsRepository: CountriesSettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filterMode = MutableStateFlow(CountriesFilterMode.ALL)
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val favouriteCodes = repository.observeFavouriteCodes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    private val requestedQuery = merge(
        flowOf(""),
        query.drop(1)
            .map(String::trim)
            .debounce(SEARCH_DEBOUNCE_MILLIS)
    ).distinctUntilChanged()

    private val remoteCountries = combine(
        requestedQuery,
        refreshRequests.onStart { emit(Unit) }
    ) { currentQuery, _ -> currentQuery }
        .flatMapLatest(::loadCountries)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RemoteCountriesState.Loading
        )

    val uiState = combine(
        query,
        filterMode,
        settingsRepository.settings,
        favouriteCodes,
        remoteCountries
    ) { rawQuery, selectedFilter, settings, codes, remoteState ->
        CountriesUiState(
            query = rawQuery,
            filterMode = selectedFilter,
            sortOrder = settings.sortOrder,
            favouritesCount = codes.size,
            content = remoteState.toContentState(
                favouriteCodes = codes,
                filterMode = selectedFilter,
                sortOrder = settings.sortOrder
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CountriesUiState()
    )

    val favouritesUiState = combine(
        repository.observeFavouriteCountries(),
        settingsRepository.settings
    ) { countries, settings ->
        val sortedCountries = sortCountries(countries, settings.sortOrder)
            .map { country -> CountryListItem(country = country, isFavourite = true) }

        if (sortedCountries.isEmpty()) {
            FavouritesUiState.Empty
        } else {
            FavouritesUiState.Success(sortedCountries)
        }
    }
        .onStart { emit(FavouritesUiState.Loading) }
        .catch { error ->
            emit(FavouritesUiState.Error("Не удалось загрузить избранное: ${error.message.orEmpty()}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FavouritesUiState.Loading
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterModeChange(value: CountriesFilterMode) {
        filterMode.value = value
    }

    fun onSortOrderChange(value: CountriesSortOrder) {
        if (uiState.value.sortOrder == value) return

        viewModelScope.launch {
            settingsRepository.setSortOrder(value)
        }
    }

    fun onRefresh() {
        refreshRequests.tryEmit(Unit)
    }

    fun retry() {
        onRefresh()
    }

    fun toggleFavourite(country: Country) {
        viewModelScope.launch {
            if (favouriteCodes.value.contains(country.code)) {
                repository.removeFavourite(country.code)
            } else {
                repository.addFavourite(country)
            }
        }
    }

    private fun loadCountries(currentQuery: String): Flow<RemoteCountriesState> = flow {
        emit(RemoteCountriesState.Loading)
        val countries = repository.fetchCountries(currentQuery)
        emit(RemoteCountriesState.Success(countries))
    }.catch { error ->
        if (error is HttpException && error.code() == 404) {
            emit(RemoteCountriesState.Success(emptyList()))
        } else {
            emit(RemoteCountriesState.Error(error.toUserMessage()))
        }
    }

    private fun RemoteCountriesState.toContentState(
        favouriteCodes: Set<String>,
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
                            isFavourite = favouriteCodes.contains(country.code)
                        )
                    }
                    .filter { item ->
                        filterMode == CountriesFilterMode.ALL || item.isFavourite
                    }
                    .let { items ->
                        when (sortOrder) {
                            CountriesSortOrder.NAME -> {
                                items.sortedBy { it.country.name.common.lowercase() }
                            }

                            CountriesSortOrder.POPULATION_DESC -> {
                                items.sortedByDescending { it.country.population }
                            }
                        }
                    }

                if (visibleCountries.isEmpty()) {
                    CountriesContentState.Empty
                } else {
                    CountriesContentState.Success(visibleCountries)
                }
            }
        }
    }

    private fun sortCountries(
        countries: List<Country>,
        sortOrder: CountriesSortOrder
    ): List<Country> {
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
        data class Success(val countries: List<Country>) : RemoteCountriesState
        data class Error(val message: String) : RemoteCountriesState
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
    }
}
