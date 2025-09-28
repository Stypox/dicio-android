package org.dicio.skill.standard.util

import org.dicio.skill.standard.capture.LocaleAndTranslation

data class MatchHelper(
    val userInput: String,
    /**
     * Used by [org.dicio.skill.standard.construct.LanguageNameCapturingConstruct].
     */
    val languageNames: List<LocaleAndTranslation>,
) {
    val splitWords = splitWords(userInput)
    val splitWordsIndices = splitWordsIndices(userInput, splitWords)
    val cumulativeWeight = cumulativeWeight(userInput, splitWords)
    val cumulativeWhitespace = cumulativeWhitespace(userInput)
    private val tokenizations: MutableMap<String, Any> = HashMap()

    fun <T> getOrTokenize(key: String, tokenizer: (MatchHelper) -> T): T {
        tokenizations[key]?.let {
            @Suppress("UNCHECKED_CAST")
            return it as T
        }

        val tokenization = tokenizer(this)
        tokenizations[key] = tokenization as Any
        return tokenization
    }
}
