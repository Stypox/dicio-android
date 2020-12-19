package org.dicio.dicio_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.LocaleListCompat;

import org.dicio.component.standard.StandardRecognizerData;
import org.dicio.dicio_android.util.LocaleUtils;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static org.dicio.dicio_android.SectionsGenerated.localeSectionsMap;
import static org.dicio.dicio_android.util.LocaleUtils.resolveSupportedLocale;

public class Sections {

    private Sections() {
    }

    private static Locale currentLocale = null;
    private static Map<String, StandardRecognizerData> sectionsMap = null;

    /**
     * Changes locale used in {@link #getSection(String)} to the available one most similar to the
     * provided one(s). To be called on app start and every time language changes. Basically
     * implements Android locale resolution.
     *
     * @param availableLocales a list of locales to try to resolve, ordered from most preferred one
     *                         to least preferred one. The first locale in the list which can be
     *                         resolved is going to be used.
     * @return the locale chosen from {@code availableLocales} and set as the sections locale. Never
     *         null.
     * @see LocaleUtils#resolveSupportedLocale(LocaleListCompat, Collection)
     * @throws LocaleUtils.UnsupportedLocaleException if the locale resolution failed
     */
    @NonNull
    public static Locale setLocale(final LocaleListCompat availableLocales)
            throws LocaleUtils.UnsupportedLocaleException {
        final LocaleUtils.LocaleResolutionResult localeResolutionResult =
                resolveSupportedLocale(availableLocales, localeSectionsMap.keySet());
        sectionsMap = localeSectionsMap.get(localeResolutionResult.supportedLocaleString);
        currentLocale = localeResolutionResult.availableLocale;
        return currentLocale;
    }

    /**
     * @return the locale corresponding to the current section map being used. Identical (i.e. the
     *         same exact object) to one of the locales inside the {@code availableLocales} list
     *         argument of {@link #setLocale(LocaleListCompat)}.
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
}
