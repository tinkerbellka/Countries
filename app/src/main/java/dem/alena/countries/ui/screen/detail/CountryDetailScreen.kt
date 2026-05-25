package dem.alena.countries.ui.screen.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    val state = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(state.country?.name?.common ?: "Страна") },
            navigationIcon = {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Назад")
                }
            }
        )

        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            state.error || state.country == null -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ошибка загрузки")
                    state.errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Повторить")
                    }
                }
            }

            else -> {
                val country = state.country!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text("Регион: ${country.region}")
                    Text("Население: ${country.population}")
                    Text("Столица: ${country.capital?.joinToString() ?: "—"}")

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(onClick = { viewModel.toggleFavourite() }) {
                            Text(if (state.isFavourite) "Убрать из избранного" else "В избранное")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Личная заметка")
                    OutlinedTextField(
                        value = state.noteText,
                        onValueChange = viewModel::onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Заметка видна в фильтре «С заметками»") },
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.saveNote() }) {
                        Text("Сохранить заметку")
                    }

                    if (state.collections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Добавить в коллекцию")
                        CollectionDropdown(
                            collections = state.collections,
                            selectedId = state.selectedCollectionId,
                            onSelected = viewModel::onCollectionSelected
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.addToSelectedCollection() },
                            enabled = state.selectedCollectionId != null
                        ) {
                            Text("Добавить в коллекцию")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionDropdown(
    collections: List<dem.alena.countries.data.repository.UserCollection>,
    selectedId: Long?,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = collections.firstOrNull { it.id == selectedId }?.name ?: "Выберите коллекцию"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            collections.forEach { collection ->
                DropdownMenuItem(
                    text = { Text(collection.name) },
                    onClick = {
                        onSelected(collection.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
