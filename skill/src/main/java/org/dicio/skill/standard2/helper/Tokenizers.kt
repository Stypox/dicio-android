package org.dicio.skill.standard2.helper

import org.dicio.skill.util.WordExtractor.nfkdNormalizeWord
import java.util.regex.Pattern


data class WordToken(
    override val start: Int,
    override val end: Int,
    val originalText: String,
    val nfkdNormalizedText: String,
) : Token

val WORD_PATTERN: Pattern = Pattern.compile("\\p{L}+")
val PUNCTUATION_PATTERN: Pattern = Pattern.compile("\\p{Punct}")
const val WORD_WEIGHT = 1.0f
const val CHAR_WEIGHT = 0.1f
const val PUNCTUATION_WEIGHT = 0.05f
const val WHITESPACE_WEIGHT = 0.0f

fun splitWords(userInput: String): List<WordToken> {
    val result: MutableList<WordToken> = ArrayList()
    val matcher = WORD_PATTERN.matcher(userInput)
    while (matcher.find()) {
        result.add(
            WordToken(
                start = matcher.start(),
                end = matcher.end(),
                originalText = matcher.group().lowercase(),
                nfkdNormalizedText = nfkdNormalizeWord(matcher.group().lowercase()),
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
    } else if (PUNCTUATION_PATTERN.matcher(c.toString()).matches()) {
        PUNCTUATION_WEIGHT
    } else {
        CHAR_WEIGHT
    }
}
