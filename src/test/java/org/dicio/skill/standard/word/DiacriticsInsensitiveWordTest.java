package org.dicio.skill.standard.word;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.dicio.skill.util.WordExtractor;
import org.junit.Test;

public class DiacriticsInsensitiveWordTest {


    private DiacriticsInsensitiveWord diw(final String value) {
        return new DiacriticsInsensitiveWord(WordExtractor.nfkdNormalizeWord(value), 0);
    }

    private void assertMatches(final String value, final String... inputWords) {
        final DiacriticsInsensitiveWord diacriticsInsensitiveWord = diw(value);
        for (final String word : inputWords) {
            assertTrue(value + " should match " + word
                            + " (normalized: " + WordExtractor.nfkdNormalizeWord(word) + ")",
                    diacriticsInsensitiveWord.matches(word, WordExtractor.nfkdNormalizeWord(word)));
        }
    }

    private void assertNotMatches(final String value, final String... inputWords) {
        final DiacriticsInsensitiveWord diacriticsInsensitiveWord = diw(value);
        for (final String word : inputWords) {
            assertFalse(value + " should not match " + word
                            + " (normalized: " + WordExtractor.nfkdNormalizeWord(word) + ")",
                    diacriticsInsensitiveWord.matches(word, WordExtractor.nfkdNormalizeWord(word)));
        }
    }


    @Test
    public void testMatches() {
        assertMatches("hello", "hèllo", "hellò", "héllò");
        assertMatches("ùlìc", "ùliç", "ulic", "ulìc");
    }

    @Test
    public void testNotMatches() {
        assertNotMatches("hello", "ħello", "hèłlo", "ħéłłò");
        assertNotMatches("ciao", "ciau", "chiao", "hiao", "chao");
    }
}
