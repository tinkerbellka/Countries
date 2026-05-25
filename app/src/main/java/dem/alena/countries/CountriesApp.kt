package dem.alena.countries

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.repository.ProfilesRepository
import dem.alena.countries.data.worker.WorkScheduler
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class CountriesApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var profilesRepository: ProfilesRepository
    @Inject lateinit var settingsRepository: CountriesSettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            profilesRepository.ensureDefaultProfile()
            val settings = settingsRepository.settings.first()
            WorkScheduler.schedule(this@CountriesApp, settings.wifiOnlyBackgroundSync)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
