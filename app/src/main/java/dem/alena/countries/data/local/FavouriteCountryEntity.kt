package dem.alena.countries.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteCountryEntity(
    @PrimaryKey val code: String
)

