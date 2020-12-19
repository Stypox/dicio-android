package org.dicio.dicio_android;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.LocaleListCompat;

import org.dicio.component.standard.StandardRecognizerData;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.dicio.dicio_android.SectionsGenerated.localeSectionsMap;

public class Sections {

    public static class UnsupportedLocaleException extends Exception {
        UnsupportedLocaleException(final Locale locale) {
            super("Unsupported locale: " + locale);
        }

        UnsupportedLocaleException() {
            super("No locales provided");
        }
    }

    private Sections() {
    }

    private static Locale currentLocale = null;
    private static Map<String, StandardRecognizerData> sectionsMap = null;

    /**
     * Changes locale used in {@link #getSection(String)} to the available one most similar to the
     * provided one(s). To be called on app start and every time language changes. Basically
     * implements Android locale resolution (not sure if exactly the same, probably, though), so it
     * tries, in this order:<br>
     * 1. language + country (e.g. {@code en-US})<br>
     * 2. parent language (e.g. {@code en})<br>
     * 3. children of parent language (e.g. {@code en-US}, {@code en-GB}, {@code en-UK}, ...) and
     *    locale names containing {@code +} (e.g. {@code b+en+001})<br>
     * @param locales a list of locales to try to resolve, ordered from most preferred one to least
     *                preferred one. The first locale in the list which can be resolved is going to
     *                be used.
     * @return the locale chosen from {@code locales} and set as the sections locale. Never null.
     * @see <a href="https://developer.android.com/guide/topics/resources/multilingual-support">
     *     Android locale resolution</a>
     * @throws UnsupportedLocaleException if the locale resolution failed
     */
    @NonNull
    public static Locale setLocale(final LocaleListCompat locales)
            throws UnsupportedLocaleException {
        UnsupportedLocaleException unsupportedLocaleException = null;
        for (int i = 0; i < locales.size(); i++) {
            try {
                final String localeString =
                        getLocaleString(locales.get(i), localeSectionsMap.keySet());
                sectionsMap = localeSectionsMap.get(localeString);
                currentLocale = locales.get(i);
                Log.i(Sections.class.getSimpleName(), "Using locale: " + localeString);
                return currentLocale;

            } catch (final UnsupportedLocaleException e) {
                if (unsupportedLocaleException == null) {
                    unsupportedLocaleException = e;
                }
            }
        }

        if (unsupportedLocaleException == null) {
            throw new UnsupportedLocaleException();
        } else {
            throw unsupportedLocaleException;
        }
    }

    /**
     * @return the locale corresponding to the current section map being used. Identical (i.e. the
     *         same exact object) to one of the locales inside the {@code locales} list argument of
     *         {@link #setLocale(LocaleListCompat)}.
     */
    @Nullable
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * @param sectionName the name of the section to obtain
     * @return the section with the provided name under the current locale
     */
    @Nullable
    public static StandardRecognizerData getSection(final String sectionName) {
        return sectionsMap.get(sectionName);
    }

    /**
     * This is package-private for testing purposes
     * @see #setLocale(LocaleListCompat)
     */
    static String getLocaleString(final Locale locale, final Set<String> supportedLocales)
            throws UnsupportedLocaleException {
        // first try with full locale name (e.g. en-US)
        String localeString = (locale.getLanguage() + "-" + locale.getCountry()).toLowerCase();
        if (supportedLocales.contains(localeString)) {
            return localeString;
        }

        // then try with only base language (e.g. en)
        localeString = locale.getLanguage().toLowerCase();
        if (supportedLocales.contains(localeString)) {
            return localeString;
        }

        // then try with children languages of locale base language (e.g. en-US, en-GB, en-UK, ...)
        for (final String supportedLocalePlus : supportedLocales) {
            for (final String supportedLocale : supportedLocalePlus.split("\\+")) {
                if (supportedLocale.split("-", 2)[0].equals(localeString)) {
                    return supportedLocalePlus;
                }
            }
        }

        // fail
        throw new UnsupportedLocaleException(locale);
    }
}
