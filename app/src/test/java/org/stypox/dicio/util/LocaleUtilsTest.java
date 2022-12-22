package org.stypox.dicio.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LocaleUtilsTest {

    String getLocaleString(final String locale, final String... supportedLocales)
            throws LocaleUtils.UnsupportedLocaleException {
        final Locale convertedLocale;
        final String[] parts = locale.split("-");
        if (parts.length == 1) {
            convertedLocale = new Locale(locale);
        } else {
            convertedLocale = new Locale(parts[0], parts[1]);
        }

        return LocaleUtils.resolveLocaleString(convertedLocale,
                new HashSet<>(Arrays.asList(supportedLocales)));
    }

    void assertLocale(final String expectedLocaleString,
                      final String locale,
                      final String... supportedLocales) throws LocaleUtils.UnsupportedLocaleException {
        assertEquals(expectedLocaleString, getLocaleString(locale, supportedLocales));
    }

    void assertLocaleNotFound(final String locale, final String... supportedLocales) {
        final String localeString;
        try {
            localeString = getLocaleString(locale, supportedLocales);
        } catch (final LocaleUtils.UnsupportedLocaleException e) {
            return;
        }
        fail("The locale \"" + locale + "\" should not have been found: " + localeString);
    }


    @Test
    public void localeTest() throws LocaleUtils.UnsupportedLocaleException {
        assertLocale("en",       "en",    "en", "en-uk", "en-gb", "it", "it-it");
        assertLocale("en-uk",    "en-UK", "en", "en-uk", "en-gb", "it", "it-it");
        assertLocale("en",       "en-US", "en", "en-uk", "en-gb", "it", "it-it");
        assertLocale("en-uk",    "en-US", "en-uk", "en-gb", "it", "it-it"); // the lexicographically bigger locale is chosen
        assertLocale("en-uk",    "en-US", "en-gb", "en-uk", "it", "it-it");
        assertLocale("it-it",    "it",    "en", "en-uk", "en-gb", "en-us", "it-it");
        assertLocale("es-rus",   "es",    "es-rus", "ru", "fr-fr");
        assertLocale("b+es+419", "es-ES", "b+es+419", "de", "fr-fr");
        assertLocale("en-rgb",   "en-US", "en-rgb", "ru", "de-de");
        assertLocale("b+en+001", "en-UK", "b+en+001", "fr", "de-de");
    }

    @Test
    public void localeCaseTest() throws LocaleUtils.UnsupportedLocaleException {
        assertLocale("it-it", "it-IT", "it", "it-it");
        assertLocale("it",    "it-IT", "it", "fr", "FR-fr");
        assertLocale("it",    "it-IT", "it", "it-IT", "fr", "FR-fr");
        assertLocaleNotFound("it", "IT", "IT-it");
    }

    @Test
    public void localeNotFoundTest() {
        assertLocaleNotFound("en", "it", "it-it", "ru", "fr-fr");
        assertLocaleNotFound("fr-CH", "it", "it-it", "it-ch", "de-ch");
        assertLocaleNotFound("de-CH", "it", "it-it", "it-ch", "fr-ch");
        assertLocaleNotFound("it-IT");
        assertLocaleNotFound("it");
    }
}
