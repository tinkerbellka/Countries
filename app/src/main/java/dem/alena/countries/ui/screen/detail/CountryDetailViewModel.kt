package dem.alena.countries.ui.screen.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.repository.CountriesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryDetailViewModel @Inject constructor(
    private val repository: CountriesRepository
) : ViewModel() {

    var country by mutableStateOf<Country?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun load(code: String) {
        viewModelScope.launch {
            isLoading = true
            error = false
            errorMessage = null
            try {
                country = repository.getCountryByCode(code)
            } catch (e: Exception) {
                error = true
                country = null
                errorMessage = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}
