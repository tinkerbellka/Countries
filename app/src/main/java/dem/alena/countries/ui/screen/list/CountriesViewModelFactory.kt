package dem.alena.countries.ui.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dem.alena.countries.data.repository.CountriesRepository


class CountriesViewModelFactory(
    private val repository: CountriesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CountriesViewModel::class.java)) {
            return CountriesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
