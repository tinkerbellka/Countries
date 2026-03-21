package dem.alena.countries.testutil

import dem.alena.countries.data.model.Country
import dem.alena.countries.data.model.Flags
import dem.alena.countries.data.model.Name
import dem.alena.countries.data.network.CountriesApi
import java.util.ArrayDeque

fun androidTestCountry(
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

class AndroidFakeCountriesApi : CountriesApi {
    private val allResponses = ArrayDeque<Result<List<Country>>>()
    private val searchResponses = mutableMapOf<String, Result<List<Country>>>()

    fun enqueueGetAllSuccess(data: List<Country>) {
        allResponses.addLast(Result.success(data))
    }

    fun enqueueGetAllError(error: Exception) {
        allResponses.addLast(Result.failure(error))
    }

    fun setSearchSuccess(query: String, data: List<Country>) {
        searchResponses[query] = Result.success(data)
    }

    override suspend fun getAllCountries(fields: String): List<Country> {
        val response = if (allResponses.isEmpty()) {
            Result.success(emptyList())
        } else {
            allResponses.removeFirst()
        }
        return response.getOrThrow()
    }

    override suspend fun searchCountries(name: String, fields: String): List<Country> {
        val response = searchResponses[name] ?: Result.success(emptyList())
        return response.getOrThrow()
    }

    override suspend fun getCountryByCode(code: String, fields: String): Country {
        return androidTestCountry(code = code, name = "Country $code")
    }
}

