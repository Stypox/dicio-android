package org.dicio.skill.old_standard.word

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.dicio.skill.old_standard_impl.word.DiacriticsInsensitiveWord
import org.dicio.skill.standard2.helper.nfkdNormalizeWord

class DiacriticsInsensitiveWordTest : StringSpec({
    "matches" {
        assertMatches("hello", "hèllo", "hellò", "héllò")
        assertMatches("ùlìc", "ùliç", "ulic", "ulìc")
    }

    "not matches" {
        assertNotMatches("hello", "ħello", "hèłlo", "ħéłłò")
        assertNotMatches("ciao", "ciau", "chiao", "hiao", "chao")
    }
})


private fun diw(value: String): DiacriticsInsensitiveWord {
    return DiacriticsInsensitiveWord(nfkdNormalizeWord(value), 0)
}

private fun assertMatches(value: String, vararg inputWords: String) {
    val diacriticsInsensitiveWord = diw(value)
    for (word in inputWords) {
        withClue("$value should match $word (normalized: ${nfkdNormalizeWord(word)})") {
            diacriticsInsensitiveWord.matches(word, nfkdNormalizeWord(word)).shouldBeTrue()
        }
    }
}

private fun assertNotMatches(value: String, vararg inputWords: String) {
    val diacriticsInsensitiveWord = diw(value)
    for (word in inputWords) {
        withClue("$value should not match $word (normalized: ${nfkdNormalizeWord(word)})") {
            diacriticsInsensitiveWord.matches(word, nfkdNormalizeWord(word)).shouldBeFalse()
        }
    }
}
