package org.dicio.skill.standard.helper

import java.text.Normalizer


data class WordToken(
    override val start: Int,
    override val end: Int,
    val originalText: String,
    val nfkdNormalizedText: String,
) : Token

private val WORD_PATTERN = Regex("\\p{L}+")
private val PUNCTUATION_PATTERN = Regex("\\p{Punct}")
private val DIACRITICAL_MARKS_REMOVER = Regex("\\p{InCombiningDiacriticalMarks}")
private const val WORD_WEIGHT = 1.0f
private const val CHAR_WEIGHT = 0.1f
private const val PUNCTUATION_WEIGHT = 0.05f
private const val WHITESPACE_WEIGHT = 0.0f



/**
 * @param word a lowercase string
 * @return the unicode NFKD normalized value for the provided word
 * @implNote the normalization process could be slow
 */
fun nfkdNormalizeWord(word: String): String {
    val normalized = Normalizer.normalize(word, Normalizer.Form.NFKD)
    return DIACRITICAL_MARKS_REMOVER.replace(normalized, "")
}

fun splitWords(userInput: String): List<WordToken> {
    val result: MutableList<WordToken> = ArrayList()
    for (match in WORD_PATTERN.findAll(userInput)) {
        result.add(
            WordToken(
                start = match.range.first,
                end = match.range.last + 1,
                originalText = match.value.lowercase(),
                nfkdNormalizedText = nfkdNormalizeWord(match.value.lowercase()),
            )
        )
    }
    return result
}

fun splitWordsIndices(userInput: String, words: List<WordToken>): IntArray {
    val result = IntArray(userInput.length + 1) { -1 }
    words.forEachIndexed { index, word ->
        result[word.start] = index
    }
    return result
}

fun cumulativeWeight(userInput: String, words: List<WordToken>): FloatArray {
    val result = FloatArray(userInput.length + 1)
    var lastEnd = 0

    for (word in words) {
        for (i in lastEnd..<word.start) {
            result[i+1] = result[i] + getCharWeight(userInput[i])
        }
        for (i in word.start..<word.end) {
            result[i+1] = result[i] + WORD_WEIGHT / (word.end - word.start)
        }
        lastEnd = word.end
    }

    for (i in lastEnd..<userInput.length) {
        result[i+1] = result[i] + getCharWeight(userInput[i])
    }
    return result
}

fun cumulativeWhitespace(userInput: String): IntArray {
    val result = IntArray(userInput.length + 1)
    for (i in userInput.indices) {
        result[i+1] = result[i] + if (userInput[i].isWhitespace()) 1 else 0
    }
    return result
}

private fun getCharWeight(c: Char): Float {
    return if (c.isWhitespace()) {
        WHITESPACE_WEIGHT
    } else if (PUNCTUATION_PATTERN.matches(c.toString())) {
        PUNCTUATION_WEIGHT
    } else {
        CHAR_WEIGHT
    }
}
