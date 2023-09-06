package org.stypox.dicio.util

import android.content.Context
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import org.stypox.dicio.R
import java.util.Locale

object LocaleUtils {
    /**
     * Basically implements Android locale resolution (not sure if exactly the same, probably,
     * though), so it tries, in this order:<br></br>
     * 1. language + country (e.g. `en-US`)<br></br>
     * 2. parent language (e.g. `en`)<br></br>
     * 3. children of parent language (e.g. `en-US`, `en-GB`, `en-UK`, ...) and
     * locale names containing `+` (e.g. `b+en+001`)<br></br>
     * @param availableLocales a list of locales to try to resolve, ordered from most preferred one
     * to least preferred one. The first locale in the list which can be
     * resolved is going to be used.
     * @param supportedLocales a set of all supported locales.
     * @return a non-null [LocaleResolutionResult] with a locale chosen from
     * `availableLocales` and the corresponding locale string.
     * @see [
     * Android locale resolution](https://developer.android.com/guide/topics/resources/multilingual-support)
     *
     * @throws UnsupportedLocaleException if the locale resolution failed.
     */
    @Throws(UnsupportedLocaleException::class)
    fun resolveSupportedLocale(
        availableLocales: LocaleListCompat,
        supportedLocales: Collection<String>
    ): LocaleResolutionResult {
        var unsupportedLocaleException: UnsupportedLocaleException? = null
        for (i in 0 until availableLocales.size()) {
            try {
                val supportedLocaleString = resolveLocaleString(
                    availableLocales[i]!!, supportedLocales
                )
                return LocaleResolutionResult(availableLocales[i]!!, supportedLocaleString)
            } catch (e: UnsupportedLocaleException) {
                if (unsupportedLocaleException == null) {
                    unsupportedLocaleException = e
                }
            }
        }
        if (unsupportedLocaleException == null) {
            throw UnsupportedLocaleException()
        } else {
            throw unsupportedLocaleException
        }
    }

    /**
     * @see resolveSupportedLocale
     */
    @JvmStatic
    @Throws(UnsupportedLocaleException::class)
    fun resolveLocaleString(
        locale: Locale,
        supportedLocales: Collection<String>
    ): String {
        // first try with full locale name (e.g. en-US)
        var localeString = (locale.language + "-" + locale.country).lowercase(Locale.getDefault())
        if (supportedLocales.contains(localeString)) {
            return localeString
        }

        // then try with only base language (e.g. en)
        localeString = locale.language.lowercase(Locale.getDefault())
        if (supportedLocales.contains(localeString)) {
            return localeString
        }

        // then try with children languages of locale base language (e.g. en-US, en-GB, en-UK, ...)
        for (supportedLocalePlus in supportedLocales) {
            for (supportedLocale in supportedLocalePlus.split("\\+".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()) {
                if (supportedLocale.split("-".toRegex(), limit = 2)
                        .toTypedArray()[0] == localeString
                ) {
                    return supportedLocalePlus
                }
            }
        }
        throw UnsupportedLocaleException(locale)
    }

    fun getAvailableLocalesFromPreferences(context: Context): LocaleListCompat {
        val language = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.pref_key_language), null)
        return if (language == null || language.trim { it <= ' ' }.isEmpty()) {
            ConfigurationCompat.getLocales(context.resources.configuration)
        } else {
            val languageCountry =
                language.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (languageCountry.size == 1) {
                LocaleListCompat.create(Locale(language))
            } else {
                LocaleListCompat.create(
                    Locale(
                        languageCountry[0],
                        languageCountry[1]
                    )
                )
            }
        }
    }

    class UnsupportedLocaleException : Exception {
        constructor(locale: Locale) : super("Unsupported locale: $locale")
        constructor() : super("No locales provided")
    }

    class LocaleResolutionResult(
        var availableLocale: Locale,
        var supportedLocaleString: String
    )
}