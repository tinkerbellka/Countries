package dem.alena.countries.data.repository

import dem.alena.countries.data.model.Country
import dem.alena.countries.data.network.CountriesApi
import dem.alena.countries.data.network.RetrofitInstance

class CountriesRepository {

    private val api: CountriesApi = RetrofitInstance.api

    suspend fun getAllCountries(): List<Country> {
        return api.getAllCountries()
    }

    suspend fun searchCountries(name: String): List<Country> {
        return api.searchCountries(name)
    }

    suspend fun getCountryByCode(code: String): Country {
        return api.getCountryByCode(code)
    }
}
