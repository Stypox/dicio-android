package org.stypox.dicio.settings

import android.app.Application
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.dataStore
import androidx.hilt.navigation.compose.hiltViewModel
import org.stypox.dicio.R
import org.stypox.dicio.settings.datastore.InputDevice
import org.stypox.dicio.settings.datastore.Language
import org.stypox.dicio.settings.datastore.SpeechOutputDevice
import org.stypox.dicio.settings.datastore.Theme
import org.stypox.dicio.settings.datastore.UserSettingsSerializer
import org.stypox.dicio.settings.ui.SettingsCategoryTitle
import org.stypox.dicio.ui.theme.AppTheme


@Composable
fun SettingsScreen(viewModel: SettingsScreenViewModel = hiltViewModel()) {
    val settings by viewModel.settingsFlow
        .collectAsState(initial = UserSettingsSerializer.defaultValue)

    LazyColumn {
        item { SettingsCategoryTitle(stringResource(R.string.pref_appearance)) }
        item {
            languageSetting().Render(
                when (val language = settings.language) {
                    Language.UNRECOGNIZED -> Language.LANGUAGE_SYSTEM
                    else -> language
                },
                viewModel::setLanguage,
            )
        }
        item {
            themeSetting().Render(
                when (val theme = settings.theme) {
                    Theme.UNRECOGNIZED -> Theme.THEME_SYSTEM
                    else -> theme
                },
                viewModel::setTheme,
            )
        }

        item { SettingsCategoryTitle(stringResource(R.string.pref_io)) }
        item {
            inputDevice().Render(
                when (val inputDevice = settings.inputDevice) {
                    InputDevice.UNRECOGNIZED,
                    InputDevice.INPUT_DEVICE_UNSET -> InputDevice.INPUT_DEVICE_VOSK
                    else -> inputDevice
                },
                viewModel::setInputDevice,
            )
        }
        item {
            speechOutputDevice().Render(
                when (val speechOutputDevice = settings.speechOutputDevice) {
                    SpeechOutputDevice.UNRECOGNIZED,
                    SpeechOutputDevice.SPEECH_OUTPUT_DEVICE_UNSET ->
                        SpeechOutputDevice.SPEECH_OUTPUT_DEVICE_ANDROID_STT
                    else -> speechOutputDevice
                },
                viewModel::setSpeechOutputDevice,
            )
        }
        item {
            sttAutoFinish().Render(
                settings.autoFinishSttService,
                viewModel::setAutoFinishSttService
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            SettingsScreen(
                SettingsScreenViewModel(
                    application = Application(),
                    dataStore = dataStore("pre", UserSettingsSerializer)
                        .getValue(LocalContext.current, SettingsScreenViewModel::settingsFlow)
                )
            )
        }
    }
}
