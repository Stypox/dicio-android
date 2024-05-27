package org.stypox.dicio.di

import android.content.Context
import android.util.Log
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.stypox.dicio.Sections
import org.stypox.dicio.settings.datastore.Language
import org.stypox.dicio.settings.datastore.UserSettings
import org.stypox.dicio.settings.datastore.UserSettingsModule.Companion.newDataStoreForPreviews
import org.stypox.dicio.util.LocaleUtils
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chooses locale and localeString from languages configured in the user's system, making sure in
 * particular that skill examples exist for the chosen locale, because otherwise the LLM wouldn't
 * work.
 */
@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    dataStore: DataStore<UserSettings>,
) {
    private fun setSectionsLocale(language: Language): Locale {
        return try {
            Sections.setLocale(
                LocaleUtils.getAvailableLocalesFromLanguage(appContext, language)
            )
        } catch (e: LocaleUtils.UnsupportedLocaleException) {
            Log.w(TAG, "Current locale is not supported, defaulting to English", e)
            try {
                // TODO ask the user to manually choose a locale instead of defaulting to english
                Sections.setLocale(LocaleListCompat.create(Locale.ENGLISH))
            } catch (e1: LocaleUtils.UnsupportedLocaleException) {
                Log.wtf(TAG, "COULD NOT LOAD THE ENGLISH LOCALE SECTIONS, IMPOSSIBLE!", e1)
                error("COULD NOT LOAD THE ENGLISH LOCALE SECTIONS, IMPOSSIBLE!")
            }
        }
    }


    private val scope = CoroutineScope(Dispatchers.Default)
    private val _locale: MutableStateFlow<Locale>
    val locale: StateFlow<Locale>

    init {
        // run blocking, because we can't start the app if we don't know the language
        var lastLanguage = runBlocking { dataStore.data.first().language }

        _locale = MutableStateFlow(setSectionsLocale(lastLanguage))
        locale = _locale

        scope.launch {
            dataStore.data
                .map { it.language }
                .collect { newLanguage ->
                    if (newLanguage != lastLanguage) {
                        lastLanguage = newLanguage
                        _locale.value = setSectionsLocale(newLanguage)
                    }
                }
        }
    }

    companion object {
        val TAG = LocaleManager::class.simpleName

        fun newForPreviews(context: Context): LocaleManager {
            return LocaleManager(
                context,
                newDataStoreForPreviews(),
            )
        }
    }
}
