package org.dicio.skill.standard.word

import org.dicio.skill.util.WordExtractor.nfkdNormalizeWord
import org.junit.Assert
import org.junit.Test

class DiacriticsInsensitiveWordTest {
    private fun diw(value: String): DiacriticsInsensitiveWord {
        return DiacriticsInsensitiveWord(nfkdNormalizeWord(value), 0)
    }

    private fun assertMatches(value: String, vararg inputWords: String) {
        val diacriticsInsensitiveWord = diw(value)
        for (word in inputWords) {
            Assert.assertTrue(
                value + " should match " + word
                        + " (normalized: " + nfkdNormalizeWord(word) + ")",
                diacriticsInsensitiveWord.matches(word, nfkdNormalizeWord(word))
            )
        }
    }

    private fun assertNotMatches(value: String, vararg inputWords: String) {
        val diacriticsInsensitiveWord = diw(value)
        for (word in inputWords) {
            Assert.assertFalse(
                value + " should not match " + word
                        + " (normalized: " + nfkdNormalizeWord(word) + ")",
                diacriticsInsensitiveWord.matches(word, nfkdNormalizeWord(word))
            )
        }
    }


    @Test
    fun testMatches() {
        assertMatches("hello", "hèllo", "hellò", "héllò")
        assertMatches("ùlìc", "ùliç", "ulic", "ulìc")
    }

    @Test
    fun testNotMatches() {
        assertNotMatches("hello", "ħello", "hèłlo", "ħéłłò")
        assertNotMatches("ciao", "ciau", "chiao", "hiao", "chao")
    }
}
