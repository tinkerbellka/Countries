package dem.alena.countries.ui.screen.list

import dem.alena.countries.data.model.Country
import dem.alena.countries.data.preferences.CountriesSortOrder
import dem.alena.countries.data.repository.DataSource

data class CountriesUiState(
    val query: String = "",
    val filterMode: CountriesFilterMode = CountriesFilterMode.ALL,
    val sortOrder: CountriesSortOrder = CountriesSortOrder.NAME,
    val favouritesCount: Int = 0,
    val activeProfileName: String = "",
    val offlineBanner: String? = null,
    val content: CountriesContentState = CountriesContentState.Loading
)

enum class CountriesFilterMode {
    ALL,
    FAVOURITES_ONLY,
    WITH_NOTES_ONLY
}

sealed interface CountriesContentState {
    data object Loading : CountriesContentState
    data object Empty : CountriesContentState
    data class Error(val message: String) : CountriesContentState
    data class Success(val countries: List<CountryListItem>) : CountriesContentState
}

data class CountryListItem(
    val country: Country,
    val isFavourite: Boolean,
    val hasNote: Boolean = false
)

sealed interface FavouritesUiState {
    data object Loading : FavouritesUiState
    data object Empty : FavouritesUiState
    data class Error(val message: String) : FavouritesUiState
    data class Success(val countries: List<CountryListItem>) : FavouritesUiState
}

fun DataSource.toBannerText(isStale: Boolean): String? {
    return when (this) {
        DataSource.CACHE -> if (isStale) {
            "Нет сети — показан устаревший кэш"
        } else {
            "Нет сети — данные из локального кэша"
        }
        DataSource.NETWORK -> null
    }
}
