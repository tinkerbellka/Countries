package dem.alena.countries.ui.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavouritesScreen(
    viewModel: CountriesViewModel,
    onCountryClick: (String) -> Unit
) {
    val state by viewModel.favouritesUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Избранное")
        Spacer(modifier = Modifier.height(16.dp))

        when (val currentState = state) {
            FavouritesUiState.Empty -> {
                Text("Пока нет избранных стран.")
            }

            FavouritesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is FavouritesUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(currentState.message)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = viewModel::onRefresh) {
                        Text("Обновить")
                    }
                }
            }

            is FavouritesUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(currentState.countries) { item ->
                        CountryCardRow(
                            item = item,
                            onClick = { onCountryClick(item.country.code) },
                            onFavouriteClick = { viewModel.toggleFavourite(item.country) }
                        )
                    }
                }
            }
        }
    }
}
