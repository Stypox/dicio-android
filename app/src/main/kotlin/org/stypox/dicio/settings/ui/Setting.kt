package org.stypox.dicio.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.stypox.dicio.R

abstract class Setting(
    protected val title: String,
    protected val icon: ImageVector? = null,
    protected val description: String? = null,
) {
    @Composable
    abstract fun ColumnScope.Render()
}

abstract class SettingWithValue<T>(
    title: String,
    icon: ImageVector? = null,
    description: String? = null,
    protected val value: T,
    protected val onValueChange: (T) -> Unit,
) : Setting(title, icon, description)

class BooleanSetting(
    title: String,
    icon: ImageVector? = null,
    description: String? = null,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) : SettingWithValue<Boolean>(title, icon, description, value, onValueChange) {
    @Composable
    override fun ColumnScope.Render() {
        SettingsItem(
            modifier = Modifier.clickable { onValueChange(!value) },
            title = title,
            icon = icon,
            description = description,
            content = {
                Switch(
                    checked = value,
                    onCheckedChange = onValueChange,
                )
            },
        )
    }
}

class ListSetting<T>(
    title: String,
    icon: ImageVector? = null,
    description: String? = null,
    value: T,
    onValueChange: (T) -> Unit,
    private val possibleValues: List<Value<T>>,
) : SettingWithValue<T>(title, icon, description, value, onValueChange) {
    data class Value<T>(
        val value: T,
        val name: String,
        val icon: ImageVector? = null,
    )

    @Composable
    override fun ColumnScope.Render() {
        var dialogOpen by rememberSaveable { mutableStateOf(false) }

        val currentValueName = possibleValues.firstOrNull { it.value == value }?.name

        SettingsItem(
            modifier = Modifier.clickable { dialogOpen = true },
            title = title,
            icon = icon,
            description = if (description == null) {
                currentValueName
            } else if (currentValueName == null) {
                description
            } else {
                stringResource(
                    id = R.string.settings_description_with_value,
                    description,
                    currentValueName
                )
            },
        )

        if (dialogOpen) {
            ChooserDialog { dialogOpen = false }
        }
    }

    @Composable
    fun ChooserDialog(onDismissRequest: () -> Unit) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                LazyColumn(
                    modifier = Modifier.padding(12.dp)
                ) {
                    item {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                        )
                    }
                    items(possibleValues) {
                        ListSettingChooserDialogItem(
                            name = it.name,
                            icon = it.icon,
                            selected = it.value == value,
                            onClick = {
                                onValueChange(it.value)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListSettingChooserDialogItem(
    name: String,
    icon: ImageVector?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = name,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
        )
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = name,
            )
        }
    }
}

@Preview
@Composable
private fun ListSettingChooserDialogItemPreview() {
    ListSettingChooserDialogItem(
        name = "Some item", icon = Icons.Default.Pets, selected = true, onClick = {})
}

@Preview
@Composable
private fun ListSettingChooserDialogPreview() {
    ListSetting(
        title = "List of things",
        value = true,
        onValueChange = {},
        possibleValues = listOf(
            ListSetting.Value(true, "True!", icon = Icons.Default.BookmarkAdded),
            ListSetting.Value(false, "False :-(", icon = Icons.Default.BookmarkRemove),
        )
    ).ChooserDialog {}
}
