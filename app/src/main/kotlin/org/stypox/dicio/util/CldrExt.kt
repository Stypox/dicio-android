package org.stypox.dicio.util

import org.dicio.skill.standard.util.nfkdNormalizeWord
import org.stypox.dicio.cldr.CldrLanguages.LocaleAndTranslation
import org.stypox.dicio.util.StringUtils.customStringDistanceCleaned

/**
 * Returns the [LocaleAndTranslation] whose name best matches [query], or `null` if none match well
 * enough.
 */
fun List<LocaleAndTranslation>.getLocaleByLanguageName(query: String): LocaleAndTranslation? {
    val normalizedQuery = nfkdNormalizeWord(query.trim())
    return this.minBy { item ->
        customStringDistanceCleaned(item.translationNormalized, normalizedQuery)
    }.takeIf { item ->
        customStringDistanceCleaned(item.translationNormalized, normalizedQuery) <= 0
    }
}

/**
 * Returns the [LocaleAndTranslation] corresponding to the provided [code], or `null` if there is no
 * translation available for [code]. In [org.stypox.dicio.cldr.CldrLanguages]'s list the more
 * relevant translations for the same language come before the alternative ones, and this function
 * will choose the more relevant one. E.g. "Central Kurdish" could alternatively be written as
 * "Kurdish, Central".
 */
fun List<LocaleAndTranslation>.codeToLanguageOrDefault(code: String): LocaleAndTranslation {
    return this.firstOrNull { lang -> lang.locale == code }
        ?: LocaleAndTranslation(code, code, code)
}
