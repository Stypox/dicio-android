package org.stypox.dicio.di

import android.content.Context
import android.util.Log
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.settings.datastore.Language
import org.stypox.dicio.settings.datastore.UserSettings
import org.stypox.dicio.settings.datastore.UserSettingsModule.Companion.newDataStoreForPreviews
import org.stypox.dicio.util.LocaleUtils
import org.stypox.dicio.util.distinctUntilChangedBlockingFirst
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
    @param:ApplicationContext private val appContext: Context,
    dataStore: DataStore<UserSettings>,
) {
    // We obtain the system locale list when the app starts (which is also when `LocaleManager` is
    // instantiated, since after `setLocale()` will be called in `BaseActivity`, the
    // `appContext.resources.configuration` will not contain the system locale anymore, but
    // only the newly set locale.
    private val systemLocaleList: LocaleListCompat =
        ConfigurationCompat.getLocales(appContext.resources.configuration)

    private val scope = CoroutineScope(Dispatchers.Default)
    private val _locale: MutableStateFlow<Locale>
    val locale: StateFlow<Locale>
    private val _sentencesLanguage: MutableStateFlow<String>
    val sentencesLanguage: StateFlow<String>

    init {
        // run blocking, because we can't start the app if we don't know the language
        val (firstLanguage, nextLanguageFlow) = dataStore.data
            .map { it.language }
            .distinctUntilChangedBlockingFirst()

        val initialResolutionResult = getSentencesLocale(firstLanguage)
        _locale = MutableStateFlow(initialResolutionResult.availableLocale)
        locale = _locale
        _sentencesLanguage = MutableStateFlow(initialResolutionResult.supportedLocaleString)
        sentencesLanguage = _sentencesLanguage

        scope.launch {
            nextLanguageFlow.collect { newLanguage ->
                val resolutionResult = getSentencesLocale(newLanguage)
                _locale.value = resolutionResult.availableLocale
                _sentencesLanguage.value = resolutionResult.supportedLocaleString
            }
        }
    }

    private fun getSentencesLocale(language: Language): LocaleUtils.LocaleResolutionResult {
        return try {
            LocaleUtils.resolveSupportedLocaleOrThrow(
                getAvailableLocalesFromLanguage(language),
                Sentences.languages
            )
        } catch (e: LocaleUtils.UnsupportedLocaleException) {
            Log.w(TAG, "Current locale is not supported, defaulting to English", e)
            // TODO ask the user to manually choose a locale instead of defaulting to english
            LocaleUtils.LocaleResolutionResult(
                availableLocale = Locale.ENGLISH,
                supportedLocaleString = "en",
            )
        }
    }

    private fun getAvailableLocalesFromLanguage(language: Language): LocaleListCompat {
        return when (language) {
            Language.LANGUAGE_SYSTEM,
            Language.UNRECOGNIZED -> {
                systemLocaleList // the original system locale list from when the app started
            }
            else -> {
                // exploit the fact that each `Language` is of the form LANGUAGE or LANGUAGE_COUNTRY
                LocaleListCompat.create(LocaleUtils.parseLanguageCountry(
                    language.toString().removePrefix("LANGUAGE_")))
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

/**
 * This module allows getting an instance of [LocaleManager] outside of @Inject using
 * `EntryPointAccessors.fromApplication()`, which can be used before an activity's `onCreate()`.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocaleManagerModule {
    fun getLocaleManager(): LocaleManager
}
