package dem.alena.countries.ui.screen.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDetailScreen(
    code: String,
    viewModel: CountryDetailViewModel,
    navController: NavController
) {
    LaunchedEffect(code) {
        viewModel.load(code)
    }

    if (viewModel.isLoading) {
        CircularProgressIndicator()
        return
    }

    if (viewModel.error || viewModel.country == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Ошибка загрузки")
            viewModel.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.load(code) }) {
                Text("Повторить")
            }
        }
        return
    }

    val country = viewModel.country!!

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(
            title = { Text(country.name.common) },
            navigationIcon = {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Назад")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Регион: ${country.region}")
        Text("Население: ${country.population}")
        Text("Столица: ${country.capital?.joinToString() ?: "—"}")
    }
}
