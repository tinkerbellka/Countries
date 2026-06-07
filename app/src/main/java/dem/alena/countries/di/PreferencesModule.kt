package dem.alena.countries.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.preferences.DataStoreCountriesSettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindCountriesSettingsRepository(
        repository: DataStoreCountriesSettingsRepository
    ): CountriesSettingsRepository
}
