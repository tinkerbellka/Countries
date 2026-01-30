package dem.alena.countries

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dem.alena.countries.ui.navigation.NavGraph
import dem.alena.countries.ui.theme.CountriesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CountriesTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
