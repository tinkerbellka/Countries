package dem.alena.countries.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dem.alena.countries.data.local.CollectionsDao
import dem.alena.countries.data.local.CountriesCacheDao
import dem.alena.countries.data.local.CountriesDatabase
import dem.alena.countries.data.local.CountryNotesDao
import dem.alena.countries.data.local.FavouritesDao
import dem.alena.countries.data.local.UserProfileDao
import dem.alena.countries.data.local.ViewHistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CountriesDatabase {
        return Room.databaseBuilder(
            context,
            CountriesDatabase::class.java,
            "countries.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideUserProfileDao(db: CountriesDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideFavouritesDao(db: CountriesDatabase): FavouritesDao = db.favouritesDao()
    @Provides fun provideCacheDao(db: CountriesDatabase): CountriesCacheDao = db.countriesCacheDao()
    @Provides fun provideViewHistoryDao(db: CountriesDatabase): ViewHistoryDao = db.viewHistoryDao()
    @Provides fun provideNotesDao(db: CountriesDatabase): CountryNotesDao = db.countryNotesDao()
    @Provides fun provideCollectionsDao(db: CountriesDatabase): CollectionsDao = db.collectionsDao()
}
