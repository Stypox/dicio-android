package org.stypox.dicio.settings.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.migrations.SharedPreferencesMigration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UserSettingsModule {
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): DataStore<UserSettings> {
        return DataStoreFactory.create(
            serializer = UserSettingsSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler {
                UserSettingsSerializer.defaultValue
            },
            migrations = listOf(
                getSharedPreferencesMigration(context),
            ),
            produceFile = {
                context.dataStoreFile("settings.pb")
            },
        )
    }

    companion object {
        fun getSharedPreferencesMigration(context: Context): SharedPreferencesMigration<UserSettings> {
            return SharedPreferencesMigration(
                context,
                context.packageName + "_preferences",
            ) { prefs, userSettings ->
                userSettings.toBuilder()
                    .setTheme(
                        when (prefs.getString("theme")) {
                            "dark" -> Theme.THEME_DARK
                            else -> Theme.THEME_SYSTEM
                        }
                    )
                    .setLanguage(
                        when (prefs.getString("language")) {
                            "cs" -> Language.LANGUAGE_CS
                            "de" -> Language.LANGUAGE_DE
                            "en" -> Language.LANGUAGE_EN
                            "en-in" -> Language.LANGUAGE_EN_IN
                            "es" -> Language.LANGUAGE_ES
                            "el" -> Language.LANGUAGE_EL
                            "fr" -> Language.LANGUAGE_FR
                            "it" -> Language.LANGUAGE_IT
                            "ru" -> Language.LANGUAGE_RU
                            "sl" -> Language.LANGUAGE_SL
                            else -> Language.LANGUAGE_SYSTEM
                        }
                    )
                    .setInputDevice(
                        when (prefs.getString("input_method")) {
                            "text" -> InputDevice.INPUT_DEVICE_NOTHING
                            else -> InputDevice.INPUT_DEVICE_UNSET
                        }
                    )
                    .setSpeechOutputDevice(
                        when (prefs.getString("speech_output_method")) {
                            "toast" -> SpeechOutputDevice.SPEECH_OUTPUT_DEVICE_TOAST
                            "snackbar" -> SpeechOutputDevice.SPEECH_OUTPUT_DEVICE_SNACKBAR
                            "nothing" -> SpeechOutputDevice.SPEECH_OUTPUT_DEVICE_NOTHING
                            else -> SpeechOutputDevice.SPEECH_OUTPUT_DEVICE_UNSET
                        }
                    )
                    .setAutoFinishSttService(
                        prefs.getBoolean("stt_auto_finish", true)
                    )
                    .build()
            }
        }

        fun newDataStoreForPreviews(): DataStore<UserSettings> {
            return object : DataStore<UserSettings> {
                private val _data = MutableStateFlow(UserSettingsSerializer.defaultValue)
                override val data: Flow<UserSettings> = _data

                override suspend fun updateData(
                    transform: suspend (t: UserSettings) -> UserSettings
                ): UserSettings {
                    val newData = transform(_data.value)
                    _data.value = newData
                    return newData
                }
            }
        }
    }
}
