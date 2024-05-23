package org.stypox.dicio.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.SettingsCategory(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
    )
    content()
}

@Preview
@Composable
private fun SettingsCategoryPreview() {
    Column {
        SettingsCategory(title = "Notifications") {
            SettingsItem(
                title = "Enable notifications",
                icon = Icons.Default.Notifications,
                description = "You should enable this option to receive download progress",
                content = {
                    Checkbox(checked = true, onCheckedChange = {})
                },
            )
            SettingsItem(
                title = "More options",
                icon = Icons.Default.SettingsApplications,
            )
        }
    }
}

@Preview
@Composable
private fun MultipleSettingsCategoryPreview() {
    Column {
        SettingsCategory(title = "Notifications") {
            SettingsItem(
                title = "Enable notifications",
                icon = Icons.Default.Notifications,
                description = "You should enable this option to receive download progress",
                content = {
                    Checkbox(checked = true, onCheckedChange = {})
                },
            )
        }

        SettingsCategory(title = "Miscellaneous") {
            SettingsItem(
                title = "Login",
                icon = Icons.AutoMirrored.Filled.Login,
                description = "Click here to login into the app",
            )
            SettingsItem(
                title = "More options",
                icon = Icons.Default.SettingsApplications,
            )
        }
    }
}
