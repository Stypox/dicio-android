package org.dicio.skill.old_standard

import org.dicio.skill.standard.util.nfkdNormalizeWord
import java.util.regex.Pattern

object WordExtractor {
    private val wordSplitter: Pattern = Pattern.compile("[^\\p{L}]+")

    /**
     * Splits the input into words at every non-letter character, and making every word lowercase.
     * <br></br>
     * For example, "Hello, how Àrè you? " becomes {"hello", "how, "àrè", "you"}
     * @param input the input from which to extract words
     * @return the list of extracted words in order
     */
    fun extractWords(input: String): List<String> {
        // match all non-letter characters
        val splitInput = wordSplitter.split(input)

        val inputWords: MutableList<String> = ArrayList()
        for (word in splitInput) {
            if (word != null && !word.isEmpty()) {
                // TODO should this be locale-sensitive?
                inputWords.add(word.lowercase())
            }
        }

        return inputWords
    }

    /**
     * Builds the list of the unicode NFKD normalized values for the input words using
     * [.nfkdNormalizeWord]
     * @param inputWords the lowercase words to normalize
     * @return the normalized words in order
     */
    fun normalizeWords(inputWords: List<String>): List<String> {
        val normalizedInputWords: MutableList<String> = ArrayList(inputWords.size)
        for (inputWord in inputWords) {
            normalizedInputWords.add(nfkdNormalizeWord(inputWord))
        }
        return normalizedInputWords
    }

    /**
     * Extracts a capturing group from the input containing the provided word range. Special
     * characters before and after the range are kept. The case and diacritics of letters are also
     * preserved.<br></br>
     * For example, extracting [1,3) from "a b, c; d " yields " b, c; " (note how spaces are also
     * kept).
     * @param input the original raw input from the user
     * @param range the range of words representing those captured in the capturing group
     * @return the content of the capturing group
     */
    fun extractCapturingGroup(input: String, range: org.dicio.skill.old_standard_impl.InputWordRange): String? {
        val pattern = Pattern.compile(
            "^(?:[^\\p{L}]*\\p{L}+){" + range.from()
                    + "}((?:[^\\p{L}]*\\p{L}+){" + (range.to() - range.from()) + "}[^\\p{L}]*)"
        )
        val matcher = pattern.matcher(input)
        val foundMatch = matcher.find()

        return if (foundMatch) {
            matcher.group(1)
        } else {
            null // unreachable, hopefully
        }
    }
}
