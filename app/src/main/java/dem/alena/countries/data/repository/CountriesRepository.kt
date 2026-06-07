package dem.alena.countries.data.repository

import dem.alena.countries.data.local.CachedCountryEntity
import dem.alena.countries.data.local.CountriesCacheDao
import dem.alena.countries.data.local.FavouriteCountryEntity
import dem.alena.countries.data.local.FavouritesDao
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.model.Flags
import dem.alena.countries.data.model.Name
import dem.alena.countries.data.network.CountriesApi
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@Singleton
class CountriesRepository @Inject constructor(
    private val api: CountriesApi,
    private val favouritesDao: FavouritesDao,
    private val cacheDao: CountriesCacheDao,
    private val settingsRepository: CountriesSettingsRepository
) {

    suspend fun fetchCountries(query: String, ttlHours: Int): CountriesFetchResult {
        val listKey = OfflineSyncPolicy.listKeyForQuery(query)
        val cachedAt = cacheDao.getLatestCachedAt(listKey)
        val cachedEntities = cacheDao.getByListKey(listKey)
        val hasCache = cachedEntities.isNotEmpty()

        return try {
            val fromNetwork = if (query.isBlank()) {
                api.getAllCountries()
            } else {
                api.searchCountries(query)
            }
            saveToCache(listKey, fromNetwork)
            CountriesFetchResult(
                countries = fromNetwork,
                source = DataSource.NETWORK,
                isStale = false,
                listKey = listKey
            )
        } catch (error: Exception) {
            if (hasCache) {
                CountriesFetchResult(
                    countries = cachedEntities.map { it.toCountry() },
                    source = DataSource.CACHE,
                    isStale = !OfflineSyncPolicy.isCacheFresh(cachedAt, ttlHours),
                    listKey = listKey
                )
            } else {
                throw error
            }
        }
    }

    suspend fun refreshAllCountriesCache(): Int {
        val listKey = OfflineSyncPolicy.ALL_COUNTRIES_LIST_KEY
        val countries = api.getAllCountries()
        saveToCache(listKey, countries)
        return countries.size
    }

    suspend fun preloadCountryDetail(code: String) {
        try {
            api.getCountryByCode(code)
        } catch (_: Exception) {
        }
    }

    suspend fun preloadFavouriteCountries(profileId: Long) {
        val favourites = favouritesDao.getAllForPreload(profileId)
        favourites.forEach { favourite ->
            preloadCountryDetail(favourite.code)
        }
    }

    suspend fun getCountryByCode(code: String, profileId: Long): Country {
        return try {
            val country = api.getCountryByCode(code)
            cacheCountrySnapshot(country)
            country
        } catch (error: Exception) {
            findOfflineCountry(code, profileId) ?: throw error
        }
    }

    private suspend fun findOfflineCountry(code: String, profileId: Long): Country? {
        return cacheDao.getByCode(code)?.toCountry()
            ?: favouritesDao.getByCode(profileId, code)?.toCountry()
    }

    private suspend fun cacheCountrySnapshot(country: Country) {
        cacheDao.insertAll(
            listOf(
                CachedCountryEntity(
                    listKey = OfflineSyncPolicy.ALL_COUNTRIES_LIST_KEY,
                    code = country.code,
                    nameCommon = country.name.common,
                    region = country.region,
                    population = country.population,
                    capital = country.capital?.firstOrNull(),
                    flagPng = country.flags.png,
                    cachedAt = System.currentTimeMillis()
                )
            )
        )
    }

    fun observeFavouriteCodes(profileId: Long): Flow<Set<String>> {
        return favouritesDao.observeAllCodes(profileId).map { it.toSet() }
    }

    suspend fun getFavouriteCodes(profileId: Long): Set<String> {
        return favouritesDao.getAllCodes(profileId).toSet()
    }

    fun observeFavouriteCountries(profileId: Long): Flow<List<Country>> {
        return favouritesDao.observeAll(profileId)
            .map { entities -> entities.map { entity -> entity.toCountry() } }
    }

    suspend fun getFavouriteCountries(profileId: Long): List<Country> {
        return favouritesDao.getAll(profileId).map { it.toCountry() }
    }

    suspend fun isFavourite(profileId: Long, code: String): Boolean {
        return favouritesDao.isFavourite(profileId, code)
    }

    suspend fun addFavourite(profileId: Long, country: Country) {
        favouritesDao.insert(
            FavouriteCountryEntity(
                profileId = profileId,
                code = country.code,
                nameCommon = country.name.common,
                region = country.region,
                population = country.population,
                capital = country.capital?.firstOrNull(),
                flagPng = country.flags.png,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeFavourite(profileId: Long, code: String) {
        favouritesDao.deleteByCode(profileId, code)
    }

    fun observeFavouriteCodesForActiveProfile(): Flow<Set<String>> {
        return settingsRepository.settings.flatMapLatest { settings ->
            val profileId = settings.activeProfileId
            if (profileId == 0L) {
                kotlinx.coroutines.flow.flowOf(emptySet())
            } else {
                observeFavouriteCodes(profileId)
            }
        }
    }

    private suspend fun saveToCache(listKey: String, countries: List<Country>) {
        val now = System.currentTimeMillis()
        cacheDao.replaceForListKey(
            listKey,
            countries.map { country ->
                CachedCountryEntity(
                    listKey = listKey,
                    code = country.code,
                    nameCommon = country.name.common,
                    region = country.region,
                    population = country.population,
                    capital = country.capital?.firstOrNull(),
                    flagPng = country.flags.png,
                    cachedAt = now
                )
            }
        )
    }

    private fun CachedCountryEntity.toCountry(): Country {
        return Country(
            code = code,
            name = Name(common = nameCommon),
            capital = capital?.let(::listOf),
            region = region,
            population = population,
            flags = Flags(png = flagPng)
        )
    }

    private fun FavouriteCountryEntity.toCountry(): Country {
        return Country(
            code = code,
            name = Name(common = nameCommon),
            capital = capital?.let(::listOf),
            region = region,
            population = population,
            flags = Flags(png = flagPng)
        )
    }
}
