package org.dicio.skill.old_standard_impl.word

/**
 * A word in a sentence with the indices of all possible subsequent words. When matching,
 * diacritics and accents will be checked (see e.g. CTRL+F -> Match Diacritics in Firefox). For
 * diacritics insensitive matching see [DiacriticsInsensitiveWord].
 *
 * @param value the value of the word, made of only unicode letters
 * @param minimumSkippedWordsToEnd the minimum number of subsequent words that have to be
 * skipped to reach the end of the sentence. Used in case the
 * end of input is reached on this word. Capturing groups count
 * as if two words were skipped.
 * @param nextIndices the indices of all possible subsequent words in the owning sentence; it
 * must always contain a value; use the length of the word array to represent
 */
class DiacriticsSensitiveWord(
    private val value: String,
    minimumSkippedWordsToEnd: Int,
    vararg nextIndices: Int
) : StringWord(minimumSkippedWordsToEnd, *nextIndices) {
    override fun matches(inputWord: String, normalizedInputWord: String): Boolean {
        return value == inputWord
    }
}
