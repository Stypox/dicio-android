package com.dicio.dicio_android.eval;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class WordExtractorTest {
    private void assertExtracts(String input, String... expectedWords) {
        List<String> words = WordExtractor.extractWords(input);
        assertArrayEquals(words.toString(), expectedWords, words.toArray());
    }

    @Test
    public void testDefault() {
        assertExtracts("\n \n\n\n hello\f   \n  \r  \thow-are  \r   you   \r\n",
                "hello", "how", "are", "you");
    }

    @Test
    public void testCase() {
        assertExtracts("Hello HOW aRe yoU",
                "hello", "how", "are", "you");
    }

    @Test
    public void testAccent() {
        assertExtracts("à è ì ò ù À È Ì Ò Ù",
                "à","è","ì","ò","ù","à","è","ì","ò","ù");
    }

    @Test
    public void testSpecialChars() {
        assertExtracts("btw! hi-hello, how are_ you??? :-) po'",
                "btw", "hi", "hello", "how", "are", "you", "po");
    }

    @Test
    public void testEmpty() {
        assertExtracts(" \r\n \f\n \r\t\t+-_");
        assertExtracts("");
    }
}