package org.dicio.skill.old_standard_impl.word

import java.util.regex.Pattern

class DiacriticsInsensitiveRegexWord(
    regex: String,
    minimumSkippedWordsToEnd: Int,
    vararg nextIndices: Int
) : StringWord(minimumSkippedWordsToEnd, *nextIndices) {
    private val regexPattern: Pattern = Pattern.compile(regex)

    override fun matches(inputWord: String, normalizedInputWord: String): Boolean {
        // match against the NFKD normalized input word
        return regexPattern.matcher(normalizedInputWord).matches()
    }
}
