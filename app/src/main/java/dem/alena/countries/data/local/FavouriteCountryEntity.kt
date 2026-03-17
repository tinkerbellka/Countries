package dem.alena.countries.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteCountryEntity(
    @PrimaryKey val code: String,
    val nameCommon: String,
    val region: String,
    val population: Long,
    val capital: String?,
    val flagPng: String
)

