package dem.alena.countries.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavouriteCountryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CountriesDatabase : RoomDatabase() {
    abstract fun favouritesDao(): FavouritesDao
}

