package dem.alena.countries.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val CACHE_REFRESH_TAG = "cache_refresh"
    private const val FAVOURITES_PRELOAD_TAG = "favourites_preload"

    fun schedule(context: Context, wifiOnly: Boolean) {
        val networkType = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()

        val cacheWork = PeriodicWorkRequestBuilder<CacheRefreshWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        val preloadWork = PeriodicWorkRequestBuilder<FavouritesPreloadWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CACHE_REFRESH_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            cacheWork
        )
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FAVOURITES_PRELOAD_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            preloadWork
        )
    }
}
