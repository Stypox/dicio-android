package org.dicio.skill.standard.word

import java.util.regex.Pattern

class DiacriticsSensitiveRegexWord(
    regex: String,
    minimumSkippedWordsToEnd: Int,
    vararg nextIndices: Int
) : StringWord(minimumSkippedWordsToEnd, *nextIndices) {
    private val regexPattern: Pattern = Pattern.compile(regex)

    override fun matches(inputWord: String, normalizedInputWord: String): Boolean {
        // match against the original input word
        return regexPattern.matcher(inputWord).matches()
    }
}
