package dem.alena.countries.data.repository

import dem.alena.countries.data.local.FavouriteCountryEntity
import dem.alena.countries.data.local.FavouritesDao
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.model.Flags
import dem.alena.countries.data.model.Name
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

    suspend fun getFavouriteCodes(): Set<String> {
        return favouritesDao.getAllCodes().toSet()
    }

    suspend fun getFavouriteCountries(): List<Country> {
        return favouritesDao.getAll().map { entity ->
            Country(
                code = entity.code,
                name = Name(common = entity.nameCommon),
                capital = entity.capital?.let { listOf(it) },
                region = entity.region,
                population = entity.population,
                flags = Flags(png = entity.flagPng)
            )
        }
    }

    suspend fun isFavourite(code: String): Boolean {
        return favouritesDao.isFavourite(code)
    }

    suspend fun addFavourite(country: Country) {
        favouritesDao.insert(
            FavouriteCountryEntity(
                code = country.code,
                nameCommon = country.name.common,
                region = country.region,
                population = country.population,
                capital = country.capital?.firstOrNull(),
                flagPng = country.flags.png
            )
        )
    }

    suspend fun removeFavourite(code: String) {
        favouritesDao.deleteByCode(code)
    }
}
