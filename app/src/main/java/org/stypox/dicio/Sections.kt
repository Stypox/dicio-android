package org.stypox.dicio

import androidx.core.os.LocaleListCompat
import org.dicio.skill.standard.StandardRecognizerData
import org.stypox.dicio.SectionsGenerated.localeSectionsMap
import org.stypox.dicio.util.LocaleUtils
import java.util.Locale

object Sections {
    /**
     * The locale corresponding to the current section map being used. Identical (i.e. the same
     * exact object) to one of the locales inside the `availableLocales` list argument of
     * [setLocale].
     */
    var currentLocale: Locale = Locale.ROOT
    private var sectionsMap: Map<String, StandardRecognizerData> = mapOf()

    /**
     * Changes locale used in [getSection] to the available one most similar to the provided one(s).
     * To be called on app start and every time language changes. Basically implements Android
     * locale resolution.
     *
     * @param availableLocales a list of locales to try to resolve, ordered from most preferred one
     * to least preferred one. The first locale in the list which can be
     * resolved is going to be used.
     * @return the locale chosen from `availableLocales` and set as the sections locale. Never
     * null.
     * @see LocaleUtils.resolveSupportedLocale
     * @throws LocaleUtils.UnsupportedLocaleException if the locale resolution failed
     */
    @Throws(LocaleUtils.UnsupportedLocaleException::class)
    fun setLocale(availableLocales: LocaleListCompat): Locale {
        val localeResolutionResult =
            LocaleUtils.resolveSupportedLocale(availableLocales, localeSectionsMap.keys)
        sectionsMap = localeSectionsMap[localeResolutionResult.supportedLocaleString]!!
        currentLocale = localeResolutionResult.availableLocale
        return currentLocale
    }

    /**
     * @param sectionName the name of the section to obtain
     * @return whether the section with the provided name exists under the current locale
     */
    fun isSectionAvailable(sectionName: String): Boolean {
        return sectionsMap.containsKey(sectionName)
    }

    /**
     * @param sectionName the name of the section to obtain
     * @return the section with the provided name under the current locale
     */
    fun getSection(sectionName: String): StandardRecognizerData? {
        return sectionsMap[sectionName]
    }
}