package dem.alena.countries.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.data.repository.ProfilesRepository
import kotlinx.coroutines.flow.first

@HiltWorker
class FavouritesPreloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val countriesRepository: CountriesRepository,
    private val profilesRepository: ProfilesRepository,
    private val settingsRepository: CountriesSettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val profileId = profilesRepository.getActiveProfileId()
            countriesRepository.preloadFavouriteCountries(profileId)
            val settings = settingsRepository.settings.first()
            countriesRepository.refreshAllCountriesCache()
            if (settings.wifiOnlyBackgroundSync) {
                Result.success()
            } else {
                Result.success()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
