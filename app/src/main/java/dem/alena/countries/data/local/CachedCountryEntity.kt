package dem.alena.countries.data.local

import androidx.room.Entity

@Entity(
    tableName = "cached_countries",
    primaryKeys = ["listKey", "code"]
)
data class CachedCountryEntity(
    val listKey: String,
    val code: String,
    val nameCommon: String,
    val region: String,
    val population: Long,
    val capital: String?,
    val flagPng: String,
    val cachedAt: Long
)
