package dem.alena.countries.ui.screen.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun FavouritesScreen(
    viewModel: CountriesViewModel,
    onCountryClick: (String) -> Unit
) {
    val state = viewModel.uiState

    if (state !is CountriesUiState.Success) {
        Text(
            text = "Нет избранных стран",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val favourites = state.countries.filter {
        viewModel.isFavourite(it.code)
    }

    if (favourites.isEmpty()) {
        Text(
            text = "Избранное пусто",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(favourites) { country ->
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
