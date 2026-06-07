package dem.alena.countries.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "countries_app_settings"
)

@Singleton
class DataStoreCountriesSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : CountriesSettingsRepository {

    override val settings: Flow<CountriesListSettings> = context.dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            CountriesListSettings(
                sortOrder = preferences[SORT_ORDER_KEY]
                    ?.let(CountriesSortOrder::valueOf)
                    ?: CountriesSortOrder.NAME,
                activeProfileId = preferences[ACTIVE_PROFILE_KEY] ?: 0L,
                themeMode = preferences[THEME_KEY]
                    ?.let(AppThemeMode::valueOf)
                    ?: AppThemeMode.SYSTEM,
                cacheTtlHours = preferences[CACHE_TTL_KEY] ?: 24,
                wifiOnlyBackgroundSync = preferences[WIFI_ONLY_KEY] ?: true
            )
        }

    override suspend fun setSortOrder(sortOrder: CountriesSortOrder) {
        context.dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = sortOrder.name
        }
    }

    override suspend fun setActiveProfileId(profileId: Long) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_PROFILE_KEY] = profileId
        }
    }

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeMode.name
        }
    }

    override suspend fun setCacheTtlHours(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[CACHE_TTL_KEY] = hours.coerceIn(1, 168)
        }
    }

    override suspend fun setWifiOnlyBackgroundSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_ONLY_KEY] = enabled
        }
    }

    private companion object {
        val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
        val ACTIVE_PROFILE_KEY = longPreferencesKey("active_profile_id")
        val THEME_KEY = stringPreferencesKey("theme_mode")
        val CACHE_TTL_KEY = intPreferencesKey("cache_ttl_hours")
        val WIFI_ONLY_KEY = booleanPreferencesKey("wifi_only_sync")
    }
}
