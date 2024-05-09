package org.dicio.skill.util

import org.dicio.skill.standard.InputWordRange
import org.dicio.skill.util.WordExtractor.extractCapturingGroup
import org.dicio.skill.util.WordExtractor.extractWords
import org.dicio.skill.util.WordExtractor.nfkdNormalizeWord
import org.dicio.skill.util.WordExtractor.normalizeWords
import org.junit.Assert
import org.junit.Test
import java.util.Arrays

class WordExtractorTest {
    @Test
    fun extractWordsTest() {
        assertExtractedWords("213heÉlo? \n\t .°- \nHOWè@ç§Ù\n+", "heélo", "howè", "ç", "ù")
        assertExtractedWords("#\tfs ùà@äöü\n°938ßÄÖÜ£&/", "fs", "ùà", "äöü", "ßäöü")
        assertExtractedWords(
            "\n \n\n\n hello\u000c   \n  \r  \thow-are  \r   you   \r\n",
            "hello",
            "how",
            "are",
            "you"
        )
        assertExtractedWords("Hello HOW aRe yoU", "hello", "how", "are", "you")
        assertExtractedWords(
            "à è ì ò ù À È Ì Ò Ù",
            "à",
            "è",
            "ì",
            "ò",
            "ù",
            "à",
            "è",
            "ì",
            "ò",
            "ù"
        )
        assertExtractedWords(" \r\n \u000c\n \r\t\t+-_")
        assertExtractedWords("")
    }

    @Test
    fun normalizeTest() {
        assertNormalized("aeiou", "aeiou", "àeiòu", "àéìoù", "aéiou")
        assertNormalized("ssèç", "ssec", "sseç", "ssèc", "ssèç", "sséc", "sséç")
    }

    @Test
    fun extractCapturingGroupTest() {
        assertExtractedCapturingGroup("", " ? - hello =? - how #are\nyou\n\t+ ", "", 0, 4)
        assertExtractedCapturingGroup("&&tàgF/sßl?fg", "+@26àèù§An/dh$ \"'938ßÄÖÜà°", "ç?&", 3, 7)
    }

    companion object {
        private fun assertExtractedWords(input: String, vararg expectedWords: String) {
            val actualWords = extractWords(input)
            Assert.assertArrayEquals(expectedWords, actualWords.toTypedArray())
        }

        fun assertNormalized(baseWord: String, vararg inputs: String) {
            val normalizedInputs =
                normalizeWords(Arrays.asList(*inputs))
            val baseCollationKey = nfkdNormalizeWord(baseWord)

            Assert.assertEquals(inputs.size.toLong(), normalizedInputs.size.toLong())
            for (i in inputs.indices) {
                Assert.assertEquals(
                    "Normalized word is different from that of " + baseWord
                            + ": " + inputs[i], baseCollationKey, normalizedInputs[i]
                )
            }
        }

        private fun assertExtractedCapturingGroup(
            left: String,
            output: String,
            right: String,
            from: Int,
            to: Int
        ) {
            val input = left + output + right
            Assert.assertEquals(
                output,
                extractCapturingGroup(input, InputWordRange(from, to))
            )
        }
    }
}
