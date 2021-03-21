package org.dicio.dicio_android.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;

import java.util.Collection;
import java.util.Locale;

public class LocaleUtils {

    public static class UnsupportedLocaleException extends Exception {
        public UnsupportedLocaleException(final Locale locale) {
            super("Unsupported locale: " + locale);
        }

        public UnsupportedLocaleException() {
            super("No locales provided");
        }
    }

    public static class LocaleResolutionResult {
        @NonNull public Locale availableLocale;
        @NonNull public String supportedLocaleString;

        public LocaleResolutionResult(@NonNull final Locale availableLocale,
                                      @NonNull final String supportedLocaleString) {
            this.availableLocale = availableLocale;
            this.supportedLocaleString = supportedLocaleString;
        }
    }

    /**
     * Basically implements Android locale resolution (not sure if exactly the same, probably,
     * though), so it tries, in this order:<br>
     * 1. language + country (e.g. {@code en-US})<br>
     * 2. parent language (e.g. {@code en})<br>
     * 3. children of parent language (e.g. {@code en-US}, {@code en-GB}, {@code en-UK}, ...) and
     *    locale names containing {@code +} (e.g. {@code b+en+001})<br>
     * @param availableLocales a list of locales to try to resolve, ordered from most preferred one
     *                         to least preferred one. The first locale in the list which can be
     *                         resolved is going to be used.
     * @param supportedLocales a set of all supported locales.
     * @return a non-null {@link LocaleResolutionResult} with a locale chosen from
     *         {@code availableLocales} and the corresponding locale string.
     * @see <a href="https://developer.android.com/guide/topics/resources/multilingual-support">
     *     Android locale resolution</a>
     * @throws UnsupportedLocaleException if the locale resolution failed.
     */
    @NonNull
    public static LocaleResolutionResult resolveSupportedLocale(
            final LocaleListCompat availableLocales,
            final Collection<String> supportedLocales)
            throws UnsupportedLocaleException {
        UnsupportedLocaleException unsupportedLocaleException = null;
        for (int i = 0; i < availableLocales.size(); i++) {
            try {
                final String supportedLocaleString =
                        resolveLocaleString(availableLocales.get(i), supportedLocales);
                return new LocaleResolutionResult(availableLocales.get(i), supportedLocaleString);
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
     * @see #resolveSupportedLocale(LocaleListCompat, Collection)
     */
    @NonNull
    static String resolveLocaleString(final Locale locale,
                                      final Collection<String> supportedLocales)
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

    public static LocaleListCompat getAvailableLocalesFromPreferences(final Context context) {
        final String language = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_key_language), null);
        if (language == null || language.trim().isEmpty()) {
            return ConfigurationCompat.getLocales(context.getResources().getConfiguration());
        } else {
            return LocaleListCompat.create(new Locale(language));
        }
    }
}
