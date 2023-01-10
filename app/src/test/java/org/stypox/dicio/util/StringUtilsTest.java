package org.stypox.dicio.util;

import static org.stypox.dicio.util.StringUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class StringUtilsTest {

    @Test
    public void defaultJoinTest() {
        assertEquals("a b c", join(Arrays.asList("a", "b", "c")));
        assertEquals("", join(Collections.emptyList()));
        assertEquals("abc", join(Collections.singletonList("abc")));
        assertEquals(" ", join(Arrays.asList("", "")));
    }

    @Test
    public void joinTest() {
        assertEquals("a-b-c", join("-", Arrays.asList("a", "b", "c")));
        assertEquals("", join("-", Collections.emptyList()));
        assertEquals("abc", join("-", Collections.singletonList("abc")));
        assertEquals("-", join("-", Arrays.asList("", "")));
    }

    @Test
    public void removePunctuationTest() {
        assertEquals("hello how are you ", removePunctuation("hello, how are you? "));
        assertEquals("12345", removePunctuation("!\"#1$%&'()*+2,-./:;<=34>?@[5]^_`{|}~"));
    }

    @Test
    public void isNullOrEmptyTest() {
        assertFalse(isNullOrEmpty("hi"));
        assertFalse(isNullOrEmpty(" \t"));
        assertFalse(isNullOrEmpty("\0"));
        //noinspection ConstantConditions
        assertTrue(isNullOrEmpty(null));
        assertTrue(isNullOrEmpty(""));
    }

    @Test
    public void levenshteinDistanceTest() {
        assertEquals(0, levenshteinDistance("kitten", "kitten"));
        assertEquals(1, levenshteinDistance("kitten", "sitten"));
        assertEquals(2, levenshteinDistance("kitten", "sittin"));
        assertEquals(3, levenshteinDistance("kitten", "sitting"));
        assertEquals(0, levenshteinDistance("dog", "dog"));
        assertEquals(4, levenshteinDistance("dòg", "caty"));
        assertEquals(2, levenshteinDistance("dog", "doggy"));
        assertEquals(2, levenshteinDistance("dog", "dosgy"));
        assertEquals(3, levenshteinDistance("dog", "dosay"));
        assertEquals(1, levenshteinDistance("dog", "doag"));
        assertEquals(1, levenshteinDistance("dog", "dogè"));
    }

    @Test
    public void levenshteinDistanceCaseTest() {
        assertEquals(3, levenshteinDistance("Kitten", "siTting"));
        assertEquals(0, levenshteinDistance("DOG", "dog"));
        assertEquals(0, levenshteinDistance("cÈd", "CèD"));
    }

    @Test
    public void levenshteinDistanceSpecialTest() {
        assertEquals(3, levenshteinDistance("abc123ABC&%$", "&%$ABCabc"));
        assertEquals(0, levenshteinDistance("abc123ABC&%$", "ABC&123%abc."));
        assertEquals(5, levenshteinDistance("email@email.email", "EMAIL#atEMAIL#dotEMAIL"));
        assertEquals(0, levenshteinDistance("Hello, hòw are you?", "hellohoware!you"));
    }

    @Test
    public void customStringDistanceTest() {
        assertEquals(-12, customStringDistance("kitten", "kitten"));
        assertEquals( -9, customStringDistance("kitten", "sitten"));
        assertEquals( -5, customStringDistance("kitten", "sittin"));
        assertEquals( -5, customStringDistance("kitten", "sitlen"));
        assertEquals( -4, customStringDistance("kitten", "sitting"));
        assertEquals( -6, customStringDistance("kitten", "sittieng"));
        assertEquals( -6, customStringDistance("dog", "dog"));
        assertEquals(  4, customStringDistance("dòg", "caty"));
        assertEquals( -3, customStringDistance("dog", "dosgy"));
        assertEquals( -1, customStringDistance("dog", "dosay"));
        assertEquals( -4, customStringDistance("dog", "doag"));
        assertEquals( -5, customStringDistance("dog", "dogè"));
    }

    @Test
    public void customStringDistanceCaseTest() {
        assertEquals(-4, customStringDistance("Kitten", "siTting"));
        assertEquals(-6, customStringDistance("DOG", "dog"));
        assertEquals(-6, customStringDistance("cÈd", "CèD"));
    }

    @Test
    public void customStringDistanceSpecialTest() {
        assertEquals( -6, customStringDistance("abc123ABC&%$", "&%$ABCabc"));
        assertEquals(-18, customStringDistance("abc123ABC&%$", "ABC&123%abc."));
        assertEquals(-20, customStringDistance("email@email.email", "EMAIL#atEMAIL#dotEMAIL"));
        assertEquals(-28, customStringDistance("Hello, hòw are you?", "hellohoware!you"));
    }

    @Test
    public void customStringDistanceRepeatedLettersTest() {
        assertEquals(-5, customStringDistance("hello", "Helloo Hiii"));
        assertEquals(-6, customStringDistance("hello", "Hello Guys"));
        assertEquals(-6, customStringDistance("hello", "Hello 2uys"));
        assertEquals(-5, customStringDistance("dog", "dogy"));
        assertEquals(-4, customStringDistance("dog", "doggy"));
    }
}
