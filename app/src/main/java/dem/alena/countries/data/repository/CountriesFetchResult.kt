package dem.alena.countries.data.repository

import dem.alena.countries.data.model.Country

enum class DataSource {
    NETWORK,
    CACHE
}

data class CountriesFetchResult(
    val countries: List<Country>,
    val source: DataSource,
    val isStale: Boolean,
    val listKey: String
)
