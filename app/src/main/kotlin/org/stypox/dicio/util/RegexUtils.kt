package org.stypox.dicio.util

import java.util.regex.Pattern

object RegexUtils {
    // taken from NewPipeExtractor, file utils/Parser.java, created by @theScrabi
    fun matchGroup(pattern: Pattern, string: String, group: Int = 1): String {
        val matcher = pattern.matcher(string)
        val foundMatch = matcher.find()
        return if (foundMatch) {
            matcher.group(group)!!
        } else {
            // only pass input to exception message when it is not too long
            if (string.length > 1024) {
                throw IllegalArgumentException("failed to find pattern \"" + pattern.pattern())
            } else {
                throw IllegalArgumentException(
                    "failed to find pattern \"" + pattern.pattern()
                            + " inside of " + string + "\""
                )
            }
        }
    }

    fun replaceAll(
        pattern: Pattern,
        string: String,
        replacement: String
    ): String {
        return pattern.matcher(string).replaceAll(replacement)
    }
}