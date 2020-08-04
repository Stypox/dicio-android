package org.dicio.dicio_android.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void defaultJoinTest() {
        assertEquals("a b c", StringUtils.join(Arrays.asList("a", "b", "c")));
        assertEquals("", StringUtils.join(Collections.emptyList()));
        assertEquals("abc", StringUtils.join(Collections.singletonList("abc")));
        assertEquals(" ", StringUtils.join(Arrays.asList("", "")));
    }

    @Test
    public void joinTest() {
        assertEquals("a-b-c", StringUtils.join("-", Arrays.asList("a", "b", "c")));
        assertEquals("", StringUtils.join("-", Collections.emptyList()));
        assertEquals("abc", StringUtils.join("-", Collections.singletonList("abc")));
        assertEquals("-", StringUtils.join("-", Arrays.asList("", "")));
    }

    @Test
    public void levenshteinDistanceTest() {
        assertEquals(0, StringUtils.levenshteinDistance("kitten", "kitten"));
        assertEquals(1, StringUtils.levenshteinDistance("kitten", "sitten"));
        assertEquals(2, StringUtils.levenshteinDistance("kitten", "sittin"));
        assertEquals(3, StringUtils.levenshteinDistance("kitten", "sitting"));
        assertEquals(0, StringUtils.levenshteinDistance("dog", "dog"));
        assertEquals(4, StringUtils.levenshteinDistance("dòg", "caty"));
        assertEquals(2, StringUtils.levenshteinDistance("dog", "doggy"));
        assertEquals(2, StringUtils.levenshteinDistance("dog", "dosgy"));
        assertEquals(3, StringUtils.levenshteinDistance("dog", "dosay"));
        assertEquals(1, StringUtils.levenshteinDistance("dog", "doag"));
        assertEquals(1, StringUtils.levenshteinDistance("dog", "dogè"));
    }

    @Test
    public void levenshteinDistanceCaseTest() {
        assertEquals(3, StringUtils.levenshteinDistance("Kitten", "siTting"));
        assertEquals(0, StringUtils.levenshteinDistance("DOG", "dog"));
        assertEquals(0, StringUtils.levenshteinDistance("cÈd", "CèD"));
    }

    @Test
    public void levenshteinDistanceSpecialTest() {
        assertEquals(0, StringUtils.levenshteinDistance("abc123ABC&%$", "ABC123abc&%$"));
        assertEquals(5, StringUtils.levenshteinDistance("email@email.email", "EMAILatEMAILdotEMAIL"));
    }

    @Test
    public void removePunctuationTest() {
        assertEquals("hello how are you ", StringUtils.removePunctuation("hello, how are you? "));
        assertEquals("12345", StringUtils.removePunctuation("!\"#1$%&'()*+2,-./:;<=34>?@[5]^_`{|}~"));
    }
}
