package dem.alena.countries.ui.screen.list

import dem.alena.countries.data.model.Country

sealed class CountriesUiState {

    object Loading : CountriesUiState()

    object Empty : CountriesUiState()

    data class Error(
        val message: String
    ) : CountriesUiState()

    data class Success(
        val countries: List<Country>
    ) : CountriesUiState()
}
