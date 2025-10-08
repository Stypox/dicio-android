package org.stypox.dicio.util

import androidx.core.os.LocaleListCompat
import org.dicio.skill.context.SkillContext
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

    /**
     * Parses a `LANGUAGE` or `LANGUAGE_COUNTRY` string string into a [Locale],
     * e.g. "EN" -> [Locale]`("en")`, "EN_IN" -> [Locale]`("en", "in")`.
     */
    fun parseLanguageCountry(languageCountry: String): Locale {
        val languageCountryArr = languageCountry
            .lowercase()
            .split("_".toRegex())
            .drop(1)
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()

        return if (languageCountryArr.size == 1) {
            Locale.Builder()
                .setLanguage(languageCountryArr[0])
                .build()
        } else {
            Locale.Builder()
                .setLanguage(languageCountryArr[0])
                .setRegion(languageCountryArr[1])
                .build()
        }
    }

    fun isLanguageSupported(ctx: SkillContext, supportedLocales: List<String>): Boolean {
        return try {
            resolveSupportedLocale(
                LocaleListCompat.create(ctx.locale),
                supportedLocales
            )
            true
        } catch (_: UnsupportedLocaleException) { false }
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
