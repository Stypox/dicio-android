package org.stypox.dicio.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

private fun getLocaleString(locale: String, vararg supportedLocales: String): String {
    return LocaleUtils.resolveLocaleString(
        LocaleUtils.parseLanguageCountry(locale),
        HashSet(listOf(*supportedLocales))
    )
}

private fun assertLocale(
    expectedLocaleString: String,
    locale: String,
    vararg supportedLocales: String
) {
    getLocaleString(locale, *supportedLocales) shouldBe expectedLocaleString
}

private fun assertLocaleNotFound(locale: String, vararg supportedLocales: String) {
    val localeString: String
    try {
        localeString = getLocaleString(locale, *supportedLocales)
    } catch (_: LocaleUtils.UnsupportedLocaleException) {
        return
    }
    error("The locale \"$locale\" should not have been found: $localeString")
}

class LocaleUtilsTest : StringSpec({
    "locale" {
        assertLocale("en", "en", "en", "en-uk", "en-gb", "it", "it-it")
        assertLocale("en-uk", "en-UK", "en", "en-uk", "en-gb", "it", "it-it")
        assertLocale("en", "en-US", "en", "en-uk", "en-gb", "it", "it-it")
        assertLocale("en-uk", "en-US", "en-uk", "en-gb", "it", "it-it") // the lexicographically bigger locale is chosen
        assertLocale("en-uk", "en-US", "en-gb", "en-uk", "it", "it-it")
        assertLocale("it-it", "it", "en", "en-uk", "en-gb", "en-us", "it-it")
        assertLocale("es-rus", "es", "es-rus", "ru", "fr-fr")
        assertLocale("b+es+419", "es-ES", "b+es+419", "de", "fr-fr")
        assertLocale("en-rgb", "en-US", "en-rgb", "ru", "de-de")
        assertLocale("b+en+001", "en-UK", "b+en+001", "fr", "de-de")
    }

    "locale with different lower/upper case" {
        assertLocale("it-it", "it-IT", "it", "it-it")
        assertLocale("it", "it-IT", "it", "fr", "FR-fr")
        assertLocale("it-IT", "it-IT", "it", "it-IT", "fr", "FR-fr")
    }

    "locale with underscores" {
        assertLocale("it_it", "it-IT", "it", "it_it")
        assertLocale("it", "it-IT", "it", "fr", "FR_fr")
        assertLocale("it_IT", "it-IT", "it", "it_IT", "fr", "FR_fr")
    }

    "locale should not be found" {
        assertLocaleNotFound("en", "it", "it-it", "ru", "fr-fr")
        assertLocaleNotFound("fr-CH", "it", "it-it", "it-ch", "de-ch")
        assertLocaleNotFound("de-CH", "it", "it-it", "it-ch", "fr-ch")
        assertLocaleNotFound("it-IT")
        assertLocaleNotFound("it")
    }
})
