package org.dicio.skill.util

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.dicio.skill.standard.InputWordRange
import org.dicio.skill.util.WordExtractor.extractCapturingGroup
import org.dicio.skill.util.WordExtractor.extractWords
import org.dicio.skill.util.WordExtractor.nfkdNormalizeWord
import org.dicio.skill.util.WordExtractor.normalizeWords


class WordExtractorTest : StringSpec({
    "extractWords" {
        assertExtractedWords("213heÉlo? \n\t .°- \nHOWè@ç§Ù\n+", "heélo", "howè", "ç", "ù")
        assertExtractedWords("#\tfs ùà@äöü\n°938ßÄÖÜ£&/", "fs", "ùà", "äöü", "ßäöü")
        assertExtractedWords("\n \n\n\n hello\u000c   \n  \r  \thow-are  \r   you   \r\n", "hello", "how", "are", "you")
        assertExtractedWords("Hello HOW aRe yoU", "hello", "how", "are", "you")
        assertExtractedWords("à è ì ò ù À È Ì Ò Ù", "à", "è", "ì", "ò", "ù", "à", "è", "ì", "ò", "ù")
        assertExtractedWords(" \r\n \u000c\n \r\t\t+-_")
        assertExtractedWords("")
    }

    "normalizeWords and nfkdNormalizeWord" {
        assertNormalized("aeiou", "aeiou", "àeiòu", "àéìoù", "aéiou")
        assertNormalized("ssèç", "ssec", "sseç", "ssèc", "ssèç", "sséc", "sséç")
    }

    "extractCapturingGroup" {
        assertExtractedCapturingGroup("", " ? - hello =? - how #are\nyou\n\t+ ", "", 0, 4)
        assertExtractedCapturingGroup("&&tàgF/sßl?fg", "+@26àèù§An/dh$ \"'938ßÄÖÜà°", "ç?&", 3, 7)
    }
})

private fun assertExtractedWords(input: String, vararg expectedWords: String) {
    extractWords(input) shouldBe expectedWords
}

private fun assertNormalized(baseWord: String, vararg inputs: String) {
    val normalizedInputs = normalizeWords(listOf(*inputs))
    val baseCollationKey = nfkdNormalizeWord(baseWord)

    normalizedInputs.size shouldBe inputs.size.toLong()
    for (i in inputs.indices) {
        withClue("Normalized word is different from that of $baseWord: ${inputs[i]}") {
            normalizedInputs[i] shouldBe baseCollationKey
        }
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
    extractCapturingGroup(input, InputWordRange(from, to)) shouldBe output
}
