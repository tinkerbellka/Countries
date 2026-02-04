package dem.alena.countries.ui.screen.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavouritesScreen(
    state: FavouritesUiState,
    onEvent: (FavouritesEvent) -> Unit,
    onCountryClick: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        onEvent(FavouritesEvent.Load)
    }

    when (state) {
        is FavouritesUiState.Empty -> {
            Text(
                text = "Избранное пусто",
                modifier = Modifier.padding(16.dp)
            )
        }
        is FavouritesUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is FavouritesUiState.Error -> {
            Text(
                text = state.message,
                modifier = Modifier.padding(16.dp)
            )
        }
        is FavouritesUiState.Success -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(state.countries) { country ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onCountryClick(country.code)
                            }
                    ) {
                        Text(
                            text = country.name.common,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
