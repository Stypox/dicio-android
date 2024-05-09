package org.stypox.dicio.util

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.stypox.dicio.util.LocaleUtils.UnsupportedLocaleException
import org.stypox.dicio.util.LocaleUtils.resolveLocaleString
import java.util.Locale

class LocaleUtilsTest {
    @Test
    @Throws(UnsupportedLocaleException::class)
    fun localeTest() {
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

    @Test
    @Throws(UnsupportedLocaleException::class)
    fun localeCaseTest() {
        assertLocale("it-it", "it-IT", "it", "it-it")
        assertLocale("it", "it-IT", "it", "fr", "FR-fr")
        assertLocale("it", "it-IT", "it", "it-IT", "fr", "FR-fr")
        assertLocaleNotFound("it", "IT", "IT-it")
    }

    @Test
    fun localeNotFoundTest() {
        assertLocaleNotFound("en", "it", "it-it", "ru", "fr-fr")
        assertLocaleNotFound("fr-CH", "it", "it-it", "it-ch", "de-ch")
        assertLocaleNotFound("de-CH", "it", "it-it", "it-ch", "fr-ch")
        assertLocaleNotFound("it-IT")
        assertLocaleNotFound("it")
    }

    companion object {
        @Throws(UnsupportedLocaleException::class)
        private fun getLocaleString(locale: String, vararg supportedLocales: String): String {
            val convertedLocale: Locale
            val parts = locale.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            convertedLocale = if (parts.size == 1) {
                Locale(locale)
            } else {
                Locale(parts[0], parts[1])
            }

            return resolveLocaleString(
                convertedLocale,
                HashSet(listOf(*supportedLocales))
            )
        }

        @Throws(UnsupportedLocaleException::class)
        private fun assertLocale(
            expectedLocaleString: String,
            locale: String,
            vararg supportedLocales: String
        ) {
            assertEquals(expectedLocaleString, getLocaleString(locale, *supportedLocales))
        }

        private fun assertLocaleNotFound(locale: String, vararg supportedLocales: String) {
            val localeString: String
            try {
                localeString = getLocaleString(locale, *supportedLocales)
            } catch (e: UnsupportedLocaleException) {
                return
            }
            fail("The locale \"$locale\" should not have been found: $localeString")
        }
    }
}
