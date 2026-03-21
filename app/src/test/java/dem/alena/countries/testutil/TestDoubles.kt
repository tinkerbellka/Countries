package dem.alena.countries.testutil

import dem.alena.countries.data.local.FavouriteCountryEntity
import dem.alena.countries.data.local.FavouritesDao
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.model.Flags
import dem.alena.countries.data.model.Name
import dem.alena.countries.data.network.CountriesApi
import java.util.ArrayDeque

fun testCountry(
    code: String = "USA",
    name: String = "United States"
): Country {
    return Country(
        code = code,
        name = Name(common = name),
        capital = listOf("Capital"),
        region = "Region",
        population = 1000L,
        flags = Flags(png = "https://example.com/$code.png")
    )
}

open class FakeCountriesApi : CountriesApi {
    private val allResponses = ArrayDeque<Result<List<Country>>>()
    private val searchResponses = mutableMapOf<String, Result<List<Country>>>()
    private val codeResponses = mutableMapOf<String, Result<Country>>()

    var getAllCalls = 0
        private set
    var searchCalls = 0
        private set

    fun enqueueGetAllSuccess(data: List<Country>) {
        allResponses.addLast(Result.success(data))
    }

    fun enqueueGetAllError(error: Exception) {
        allResponses.addLast(Result.failure(error))
    }

    fun setSearchSuccess(query: String, data: List<Country>) {
        searchResponses[query] = Result.success(data)
    }

    fun setSearchError(query: String, error: Exception) {
        searchResponses[query] = Result.failure(error)
    }

    fun setCountrySuccess(code: String, country: Country) {
        codeResponses[code] = Result.success(country)
    }

    override suspend fun getAllCountries(fields: String): List<Country> {
        getAllCalls++
        val response = if (allResponses.isEmpty()) {
            Result.success(emptyList())
        } else {
            allResponses.removeFirst()
        }
        return response.getOrThrow()
    }

    override suspend fun searchCountries(name: String, fields: String): List<Country> {
        searchCalls++
        val response = searchResponses[name] ?: Result.success(emptyList())
        return response.getOrThrow()
    }

    override suspend fun getCountryByCode(code: String, fields: String): Country {
        val response = codeResponses[code]
            ?: Result.failure(IllegalStateException("No fake response for code=$code"))
        return response.getOrThrow()
    }
}

class FakeFavouritesDao : FavouritesDao {
    private val items = linkedMapOf<String, FavouriteCountryEntity>()

    override suspend fun getAllCodes(): List<String> = items.keys.toList()

    override suspend fun getAll(): List<FavouriteCountryEntity> = items.values.toList()

    override suspend fun isFavourite(code: String): Boolean = items.containsKey(code)

    override suspend fun insert(entity: FavouriteCountryEntity) {
        items[entity.code] = entity
    }

    override suspend fun deleteByCode(code: String) {
        items.remove(code)
    }
}

