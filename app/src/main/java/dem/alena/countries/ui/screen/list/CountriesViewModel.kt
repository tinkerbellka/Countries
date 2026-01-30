package dem.alena.countries.ui.screen.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.repository.CountriesRepository
import kotlinx.coroutines.launch

class CountriesViewModel : ViewModel() {

    private val repository = CountriesRepository()

    var uiState by mutableStateOf<CountriesUiState>(CountriesUiState.Loading)
        private set

    private var favourites by mutableStateOf<Set<String>>(emptySet())
        private set

    init {
        loadAllCountries()
    }

    private fun loadAllCountries() {
        uiState = CountriesUiState.Loading

        viewModelScope.launch {
            try {
                val result = repository.getAllCountries()
                uiState = if (result.isEmpty()) {
                    CountriesUiState.Empty
                } else {
                    CountriesUiState.Success(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = CountriesUiState.Error(
                    "Не удалось загрузить список стран: ${e.javaClass.simpleName} ${e.message ?: ""}"
                )
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            loadAllCountries()
            return
        }

        uiState = CountriesUiState.Loading

        viewModelScope.launch {
            try {
                val result = repository.searchCountries(query)

                uiState = if (result.isEmpty()) {
                    CountriesUiState.Empty
                } else {
                    CountriesUiState.Success(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = CountriesUiState.Error(
                    "Не удалось загрузить страны: ${e.javaClass.simpleName} ${e.message ?: ""}"
                )
            }
        }
    }

    fun toggleFavourite(country: Country) {
        favourites = if (favourites.contains(country.code)) {
            favourites - country.code
        } else {
            favourites + country.code
        }
    }

    fun isFavourite(code: String): Boolean {
        return favourites.contains(code)
    }
}
