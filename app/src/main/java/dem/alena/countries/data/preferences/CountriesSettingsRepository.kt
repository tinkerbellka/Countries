package dem.alena.countries.data.preferences

import kotlinx.coroutines.flow.Flow

interface CountriesSettingsRepository {
    val settings: Flow<CountriesListSettings>

    suspend fun setSortOrder(sortOrder: CountriesSortOrder)

    suspend fun setActiveProfileId(profileId: Long)

    suspend fun setThemeMode(themeMode: AppThemeMode)

    suspend fun setCacheTtlHours(hours: Int)

    suspend fun setWifiOnlyBackgroundSync(enabled: Boolean)
}
