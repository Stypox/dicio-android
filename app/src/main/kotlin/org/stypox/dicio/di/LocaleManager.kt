package org.stypox.dicio.di

import android.content.Context
import android.util.Log
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
import org.stypox.dicio.sentences.Sentences
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
    private fun getSentencesLocale(language: Language): LocaleUtils.LocaleResolutionResult {
        return try {
            LocaleUtils.resolveSupportedLocale(
                LocaleUtils.getAvailableLocalesFromLanguage(appContext, language),
                Sentences.languages
            )
        } catch (e: LocaleUtils.UnsupportedLocaleException) {
            Log.w(TAG, "Current locale is not supported, defaulting to English", e)
            try {
                // TODO ask the user to manually choose a locale instead of defaulting to english
                LocaleUtils.LocaleResolutionResult(
                    availableLocale = Locale.ENGLISH,
                    supportedLocaleString = "en",
                )
            } catch (e1: LocaleUtils.UnsupportedLocaleException) {
                Log.wtf(TAG, "COULD NOT LOAD THE ENGLISH LOCALE SECTIONS, IMPOSSIBLE!", e1)
                error("COULD NOT LOAD THE ENGLISH LOCALE SECTIONS, IMPOSSIBLE!")
            }
        }
    }


    private val scope = CoroutineScope(Dispatchers.Default)
    private val _locale: MutableStateFlow<Locale>
    val locale: StateFlow<Locale>
    private val _sentencesLanguage: MutableStateFlow<String>
    val sentencesLanguage: StateFlow<String>

    init {
        // run blocking, because we can't start the app if we don't know the language
        var lastLanguage = runBlocking { dataStore.data.first().language }

        val initialResolutionResult = getSentencesLocale(lastLanguage)
        _locale = MutableStateFlow(initialResolutionResult.availableLocale)
        locale = _locale
        _sentencesLanguage = MutableStateFlow(initialResolutionResult.supportedLocaleString)
        sentencesLanguage = _sentencesLanguage

        scope.launch {
            dataStore.data
                .map { it.language }
                .collect { newLanguage ->
                    if (newLanguage != lastLanguage) {
                        lastLanguage = newLanguage
                        val resolutionResult = getSentencesLocale(newLanguage)
                        _locale.value = resolutionResult.availableLocale
                        _sentencesLanguage.value = resolutionResult.supportedLocaleString
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
