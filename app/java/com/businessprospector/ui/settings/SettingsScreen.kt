package com.businessprospector.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // API i Integracje
            SettingsSection(title = "API & Integrations") {
                SettingsItem(
                    icon = Icons.Default.Search,
                    title = "Google Search API",
                    subtitle = if (settings.googleApiConfigured) "Configured" else "Not configured",
                    onClick = { navController.navigate("settings/api_config/google") }
                )

                SettingsItem(
                    icon = Icons.Default.DataObject,
                    title = "Language Models API",
                    subtitle = if (settings.llmApiConfigured) "Configured" else "Not configured",
                    onClick = { navController.navigate("settings/api_config/llm") }
                )

                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Email Settings",
                    subtitle = if (settings.emailConfigured) "Configured" else "Not configured",
                    onClick = { navController.navigate("settings/api_config/email") }
                )

                SettingsItem(
                    icon = Icons.Default.Phone,
                    title = "SMS Settings",
                    subtitle = if (settings.smsConfigured) "Configured" else "Not configured",
                    onClick = { navController.navigate("settings/api_config/sms") }
                )
            }

            // Prywatność i Bezpieczeństwo
            SettingsSection(title = "Privacy & Security") {
                var encryptionEnabled by remember { mutableStateOf(settings.dataEncryptionEnabled) }

                SettingsToggleItem(
                    icon = Icons.Default.Lock,
                    title = "Data Encryption",
                    subtitle = "Encrypt sensitive contact data",
                    checked = encryptionEnabled,
                    onCheckedChange = {
                        encryptionEnabled = it
                        viewModel.updateDataEncryption(it)
                    }
                )

                var gdprEnabled by remember { mutableStateOf(settings.gdprCompliance) }

                SettingsToggleItem(
                    icon = Icons.Default.Info,
                    title = "GDPR Compliance",
                    subtitle = "Enable GDPR data protection features",
                    checked = gdprEnabled,
                    onCheckedChange = {
                        gdprEnabled = it
                        viewModel.updateGdprCompliance(it)
                    }
                )
            }

            // Powiadomienia i Automatyzacja
            SettingsSection(title = "Notifications & Automation") {
                var notificationsEnabled by remember { mutableStateOf(settings.notificationsEnabled) }

                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Receive notifications about responses and events",
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        viewModel.updateNotifications(it)
                    }
                )

                SettingsItem(
                    icon = Icons.Default.RestoreFromTrash,
                    title = "Backup & Restore",
                    subtitle = "Backup your data or restore from backup",
                    onClick = { navController.navigate("settings/backup") }
                )

                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Storage Management",
                    subtitle = "Manage app storage and data",
                    onClick = { navController.navigate("settings/storage") }
                )
            }

            // Konto
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Profile Settings",
                    subtitle = "Manage your profile information",
                    onClick = { navController.navigate("settings/profile") }
                )
            }

            // Informacje o Aplikacji
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Divider(
        modifier = Modifier.padding(start = 56.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }

    Divider(
        modifier = Modifier.padding(start = 56.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}