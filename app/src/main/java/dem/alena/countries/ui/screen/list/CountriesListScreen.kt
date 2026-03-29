package dem.alena.countries.ui.screen.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dem.alena.countries.data.preferences.CountriesSortOrder

@Composable
fun CountriesListScreen(
    viewModel: CountriesViewModel,
    onCountryClick: (String) -> Unit,
    onFavouritesClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Страны")
            Button(onClick = onFavouritesClick) {
                Text("Избранное")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Поиск") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.filterMode == CountriesFilterMode.ALL,
                onClick = { viewModel.onFilterModeChange(CountriesFilterMode.ALL) },
                label = { Text("Все") }
            )
            FilterChip(
                selected = state.filterMode == CountriesFilterMode.FAVOURITES_ONLY,
                onClick = { viewModel.onFilterModeChange(CountriesFilterMode.FAVOURITES_ONLY) },
                label = { Text("Только избранное") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.sortOrder == CountriesSortOrder.NAME,
                onClick = { viewModel.onSortOrderChange(CountriesSortOrder.NAME) },
                label = { Text("По названию") }
            )
            FilterChip(
                selected = state.sortOrder == CountriesSortOrder.POPULATION_DESC,
                onClick = { viewModel.onSortOrderChange(CountriesSortOrder.POPULATION_DESC) },
                label = { Text("По населению") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = viewModel::onRefresh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Обновить")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Избранных стран: ${state.favouritesCount}")

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val content = state.content) {
                CountriesContentState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is CountriesContentState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(content.message)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = viewModel::retry) {
                            Text("Повторить")
                        }
                    }
                }

                CountriesContentState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text("Ничего не найдено. Попробуйте другой запрос или фильтр.")
                    }
                }

                is CountriesContentState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(content.countries) { item ->
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
}

@Composable
fun CountryCardRow(
    item: CountryListItem,
    onClick: () -> Unit,
    onFavouriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.country.name.common)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${item.country.region} • ${item.country.population}")
            }
            IconButton(onClick = onFavouriteClick) {
                Text(if (item.isFavourite) "★" else "☆")
            }
        }
    }
}
