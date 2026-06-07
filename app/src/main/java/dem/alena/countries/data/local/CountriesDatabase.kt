package dem.alena.countries.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        FavouriteCountryEntity::class,
        CachedCountryEntity::class,
        ViewHistoryEntity::class,
        CountryNoteEntity::class,
        CollectionEntity::class,
        CollectionCountryCrossRef::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CountriesDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun favouritesDao(): FavouritesDao
    abstract fun countriesCacheDao(): CountriesCacheDao
    abstract fun viewHistoryDao(): ViewHistoryDao
    abstract fun countryNotesDao(): CountryNotesDao
    abstract fun collectionsDao(): CollectionsDao
}
