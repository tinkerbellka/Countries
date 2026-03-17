package dem.alena.countries.ui.screen.list

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.repository.CountriesRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val repository: CountriesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CountriesViewModel"
    }

    var uiState by mutableStateOf<CountriesUiState>(CountriesUiState.Loading)
        private set

    var favouritesState by mutableStateOf<FavouritesUiState>(FavouritesUiState.Empty)
        private set

    private var favourites by mutableStateOf<Set<String>>(emptySet())
        private set

    init {
        loadAllCountries()
        loadFavouriteCodes()
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
                Log.e(TAG, "Не удалось загрузить список стран", e)
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
            } catch (e: HttpException) {
                Log.e(TAG, "Поиск стран: HttpException", e)
                uiState = if (e.code() == 404) {
                    CountriesUiState.Empty
                } else {
                    CountriesUiState.Error(
                        "Не удалось загрузить страны: ${e.javaClass.simpleName} ${e.message ?: ""}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Поиск стран: ошибка", e)
                uiState = CountriesUiState.Error(
                    "Не удалось загрузить страны: ${e.javaClass.simpleName} ${e.message ?: ""}"
                )
            }
        }
    }

    fun toggleFavourite(country: Country) {
        viewModelScope.launch {
            val isCurrentlyFavourite = repository.isFavourite(country.code)
            if (isCurrentlyFavourite) {
                repository.removeFavourite(country.code)
                favourites = favourites - country.code
            } else {
                repository.addFavourite(country)
                favourites = favourites + country.code
            }
        }
    }

    fun isFavourite(code: String): Boolean {
        return favourites.contains(code)
    }

    private fun loadFavouriteCodes() {
        viewModelScope.launch {
            try {
                favourites = repository.getFavouriteCodes()
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось загрузить избранное из БД", e)
            }
        }
    }

    fun onFavouritesEvent(event: FavouritesEvent) {
        when (event) {
            FavouritesEvent.Load -> loadFavourites()
        }
    }

    private fun loadFavourites() {
        favouritesState = FavouritesUiState.Loading
        viewModelScope.launch {
            try {
                val result = repository.getFavouriteCountries()
                favouritesState = if (result.isEmpty()) {
                    FavouritesUiState.Empty
                } else {
                    FavouritesUiState.Success(result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось загрузить избранное", e)
                favouritesState = FavouritesUiState.Error(
                    "Не удалось загрузить избранное: ${e.message ?: ""}"
                )
            }
        }
    }
}

sealed class FavouritesUiState {
    data object Empty : FavouritesUiState()
    data object Loading : FavouritesUiState()
    data class Success(val countries: List<Country>) : FavouritesUiState()
    data class Error(val message: String) : FavouritesUiState()
}

sealed interface FavouritesEvent {
    data object Load : FavouritesEvent
}
