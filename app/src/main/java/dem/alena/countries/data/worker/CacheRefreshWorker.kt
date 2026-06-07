package dem.alena.countries.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dem.alena.countries.data.repository.CountriesRepository
import dem.alena.countries.data.repository.ProfilesRepository

@HiltWorker
class CacheRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val countriesRepository: CountriesRepository,
    private val profilesRepository: ProfilesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            profilesRepository.ensureDefaultProfile()
            val count = countriesRepository.refreshAllCountriesCache()
            if (count > 0) Result.success() else Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
