package org.dicio.dicio_android.sentences;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SectionsTest {

    String getLocaleString(final String locale, final String... supportedLocales)
            throws Sections.UnsupportedLocaleException {
        final Locale convertedLocale;
        final String[] parts = locale.split("-");
        if (parts.length == 1) {
            convertedLocale = new Locale(locale);
        } else {
            convertedLocale = new Locale(parts[0], parts[1]);
        }

        return Sections.getLocaleString(convertedLocale,
                new HashSet<>(Arrays.asList(supportedLocales)));
    }

    void assertLocale(final String expectedLocaleString,
                      final String locale,
                      final String... supportedLocales) throws Sections.UnsupportedLocaleException {
        assertEquals(expectedLocaleString, getLocaleString(locale, supportedLocales));
    }

    void assertLocaleNotFound(final String locale, final String... supportedLocales) {
        final String localeString;
        try {
            localeString = getLocaleString(locale, supportedLocales);
        } catch (final Sections.UnsupportedLocaleException e) {
            return;
        }
        fail("The locale \"" + locale + "\" should not have been found: " + localeString);
    }


    @Test
    public void localeFoundTest() throws Sections.UnsupportedLocaleException {
        assertLocale("en",       "en",    "en", "en-UK", "en-GB", "it", "it-IT");
        assertLocale("en-UK",    "en-UK", "en", "en-UK", "en-GB", "it", "it-IT");
        assertLocale("en",       "en-US", "en", "en-UK", "en-GB", "it", "it-IT");
        assertLocale("en-UK",    "en-US", "en-UK", "en-GB", "it", "it-IT"); // the lexicographically bigger locale is chosen
        assertLocale("en-UK",    "en-US", "en-GB", "en-UK", "it", "it-IT");
        assertLocale("it-IT",    "it",    "en", "en-UK", "en-GB", "en-US", "it-IT");
        assertLocale("es-rUS",   "es",    "es-rUS", "ru", "fr-FR");
        assertLocale("b+es+419", "es-ES", "b+es+419", "de", "fr-FR");
        assertLocale("en-rGB",   "en-US", "en-rGB", "ru", "de-DE");
        assertLocale("b+en+001", "en-UK", "b+en+001", "fr", "de-DE");
    }

    @Test
    public void localeNotFoundTest() {
        assertLocaleNotFound("en", "it", "it-IT", "ru", "fr-FR");
        assertLocaleNotFound("fr-CH", "it", "it-IT", "it-CH", "de-CH");
        assertLocaleNotFound("de-CH", "it", "it-IT", "it-CH", "fr-CH");
        assertLocaleNotFound("it-IT");
        assertLocaleNotFound("it");
    }
}
