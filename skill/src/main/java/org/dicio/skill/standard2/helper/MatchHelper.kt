package org.dicio.skill.standard2.helper

data class MatchHelper(
    val userInput: String,
) {
    val splitWords = splitWords(userInput)
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
