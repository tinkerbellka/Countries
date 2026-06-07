package dem.alena.countries.testutil

import dem.alena.countries.data.preferences.AppThemeMode
import dem.alena.countries.data.preferences.CountriesListSettings
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.preferences.CountriesSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AndroidFakeCountriesSettingsRepository(
    initialSettings: CountriesListSettings = CountriesListSettings(activeProfileId = 1L)
) : CountriesSettingsRepository {
    private val settingsFlow = MutableStateFlow(initialSettings)

    override val settings: Flow<CountriesListSettings> = settingsFlow

    override suspend fun setSortOrder(sortOrder: CountriesSortOrder) {
        settingsFlow.value = settingsFlow.value.copy(sortOrder = sortOrder)
    }

    override suspend fun setActiveProfileId(profileId: Long) {
        settingsFlow.value = settingsFlow.value.copy(activeProfileId = profileId)
    }

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        settingsFlow.value = settingsFlow.value.copy(themeMode = themeMode)
    }

    override suspend fun setCacheTtlHours(hours: Int) {
        settingsFlow.value = settingsFlow.value.copy(cacheTtlHours = hours)
    }

    override suspend fun setWifiOnlyBackgroundSync(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(wifiOnlyBackgroundSync = enabled)
    }
}
