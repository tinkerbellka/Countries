package dem.alena.countries.data.repository

import dem.alena.countries.data.local.FavouriteCountryEntity
import dem.alena.countries.data.local.FavouritesDao
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.network.CountriesApi
import javax.inject.Inject

class CountriesRepository @Inject constructor(
    private val api: CountriesApi,
    private val favouritesDao: FavouritesDao
) {

    suspend fun getAllCountries(): List<Country> {
        return api.getAllCountries()
    }

    suspend fun searchCountries(name: String): List<Country> {
        return api.searchCountries(name)
    }

    suspend fun getCountryByCode(code: String): Country {
        return api.getCountryByCode(code)
    }

    suspend fun getCountriesByCodes(codes: List<String>): List<Country> {
        return codes.mapNotNull { code ->
            try {
                api.getCountryByCode(code)
            } catch (_: Exception) {
                null
            }
        }
    }

    //избранные с Room
    suspend fun getFavouriteCodes(): Set<String> {
        return favouritesDao.getAllCodes().toSet()
    }

    suspend fun isFavourite(code: String): Boolean {
        return favouritesDao.isFavourite(code)
    }

    suspend fun addFavourite(code: String) {
        favouritesDao.insert(FavouriteCountryEntity(code))
    }

    suspend fun removeFavourite(code: String) {
        favouritesDao.delete(FavouriteCountryEntity(code))
    }
}
