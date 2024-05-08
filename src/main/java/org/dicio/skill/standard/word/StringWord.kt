package org.dicio.skill.standard.word

/**
 * A word in a sentence with the indices of all possible subsequent words. Use
 * [DiacriticsSensitiveWord] for diacritics-sensitive matching and
 * [DiacriticsInsensitiveWord] for diacritics-insensitive matching.
 *
 * @param minimumSkippedWordsToEnd the minimum number of subsequent words that have to be
 * skipped to reach the end of the sentence. Used in case the
 * end of input is reached on this word. Capturing groups count
 * as if two words were skipped.
 * @param nextIndices the indices of all possible subsequent words in the owning sentence; it
 * must always contain a value; use the length of the word array to represent
 */
abstract class StringWord(minimumSkippedWordsToEnd: Int, vararg nextIndices: Int) :
    BaseWord(minimumSkippedWordsToEnd, *nextIndices) {
    /**
     * @param inputWord the input word, made of only unicode letters
     * @param normalizedInputWord the unicode NFKD normalized value for the input word. Use
     * [org.dicio.skill.util.WordExtractor.nfkdNormalizeWord]
     * to NFKD normalize a word.
     * @return whether the input word matches this word or not
     */
    abstract fun matches(inputWord: String, normalizedInputWord: String): Boolean
}
