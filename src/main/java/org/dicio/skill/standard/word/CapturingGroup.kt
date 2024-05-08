package org.dicio.skill.standard.word

/**
 * A capturing group in a sentence with the indices of all possible subsequent base words
 *
 * @param name the capturing group name, used for identification purposes
 * @param minimumSkippedWordsToEnd the minimum number of subsequent words that have to be
 * skipped to reach the end of the sentence. Used in case the
 * end of input is reached on this word. Capturing groups count
 * as if two words were skipped.
 * @param nextIndices the indices of all possible subsequent words in the owning sentence; it
 * must always contain a value; use the length of the word array to represent
 */
class CapturingGroup(
    val name: String,
    minimumSkippedWordsToEnd: Int,
    vararg nextIndices: Int
) : BaseWord(minimumSkippedWordsToEnd, *nextIndices)
