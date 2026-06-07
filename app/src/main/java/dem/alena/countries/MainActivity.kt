package dem.alena.countries

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import dem.alena.countries.ui.navigation.CountriesAppNavHost
import dem.alena.countries.ui.screen.settings.SettingsViewModel
import dem.alena.countries.ui.theme.CountriesTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.uiState.collectAsState()
            CountriesTheme(themeMode = settings.settings.themeMode) {
                CountriesAppNavHost()
            }
        }
    }
}
