package dem.alena.countries.data.network

import dem.alena.countries.data.model.Country
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CountriesApi {

    @GET("all")
    suspend fun getAllCountries(
        @Query("fields") fields: String =
            "cca3,name,capital,region,population,flags"
    ): List<Country>

    @GET("name/{name}")
    suspend fun searchCountries(
        @Path("name") name: String,
        @Query("fields") fields: String =
            "cca3,name,capital,region,population,flags"
    ): List<Country>

    @GET("alpha/{code}")
    suspend fun getCountryByCode(
        @Path("code") code: String,
        @Query("fields") fields: String =
            "cca3,name,capital,region,population,flags"
    ): Country
}
