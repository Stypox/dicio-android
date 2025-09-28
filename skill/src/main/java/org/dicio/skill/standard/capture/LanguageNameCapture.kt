package org.dicio.skill.standard.capture

data class LanguageNameCapture(
    override val name: String,
    val localeAndTranslation: LocaleAndTranslation,
) : NamedCapture

/**
 * A data class holding a locale code along with the corresponding translated language name
 */
data class LocaleAndTranslation(
    /**
     * The locale code for the language, lowercase and with underscores separating variants (e.g.
     * "en" or "zh_hans")
     */
    val locale: String,
    /**
     * The translated name of this language
     */
    val translation: String,
    /**
     * The translated name of this language (NFKD-normalized)
     */
    val translationNormalized: String,
)
