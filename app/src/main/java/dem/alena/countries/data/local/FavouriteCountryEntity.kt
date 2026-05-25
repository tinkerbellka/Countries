package dem.alena.countries.data.local

import androidx.room.Entity

@Entity(
    tableName = "favourites",
    primaryKeys = ["profileId", "code"]
)
data class FavouriteCountryEntity(
    val profileId: Long,
    val code: String,
    val nameCommon: String,
    val region: String,
    val population: Long,
    val capital: String?,
    val flagPng: String,
    val addedAt: Long
)
