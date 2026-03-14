package dem.alena.countries.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dem.alena.countries.data.local.CountriesDatabase
import dem.alena.countries.data.local.FavouritesDao
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavouritesDao(
        database: CountriesDatabase
    ): FavouritesDao = database.favouritesDao()
}

