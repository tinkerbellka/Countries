package dem.alena.countries.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dem.alena.countries.data.preferences.AppThemeMode
import dem.alena.countries.data.repository.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val settings = state.settings
    val activeProfile = state.profiles.firstOrNull { it.isActive }

    if (state.pinDialogProfileId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPinDialog() },
            title = { Text("Вход в «${state.pinDialogProfileName}»") },
            text = {
                OutlinedTextField(
                    value = state.enteredPin,
                    onValueChange = viewModel::onEnteredPinChange,
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmPinDialog() }) {
                    Text("Войти")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPinDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Профили на этом устройстве", style = MaterialTheme.typography.titleMedium)
        Text(
            "У каждого профиля своё избранное, история и коллекции. PIN защищает переключение.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        state.profiles.forEach { profile ->
            ProfileCard(
                profile = profile,
                onSelect = { viewModel.requestSwitchProfile(profile) },
                onDelete = { viewModel.deleteProfile(profile.id) },
                canDelete = state.profiles.size > 1 && !profile.isActive
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Новый профиль", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = state.newProfileName,
                    onValueChange = viewModel::onNewProfileNameChange,
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.newProfilePin,
                    onValueChange = viewModel::onNewProfilePinChange,
                    label = { Text("PIN (необязательно, 4–6 цифр)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = { viewModel.createProfile() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Создать")
                }
            }
        }

        state.message?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Приватность", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Показывать мои коллекции другим")
                Text(
                    "Профиль «${activeProfile?.name ?: "—"}»: другие пользователи увидят ваши коллекции во вкладке «Общие».",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = activeProfile?.shareCollections == true,
                onCheckedChange = viewModel::onShareCollectionsChange,
                enabled = activeProfile != null
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Тема", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = settings.themeMode == mode,
                    onClick = { viewModel.setThemeMode(mode) },
                    label = {
                        Text(
                            when (mode) {
                                AppThemeMode.SYSTEM -> "Системная"
                                AppThemeMode.LIGHT -> "Светлая"
                                AppThemeMode.DARK -> "Тёмная"
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: UserProfile,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                val extras = buildList {
                    if (profile.isActive) add("активный")
                    if (profile.hasPin) add("с PIN")
                    if (profile.shareCollections) add("коллекции открыты")
                }
                if (extras.isNotEmpty()) {
                    Text(
                        extras.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (canDelete) {
                TextButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}
