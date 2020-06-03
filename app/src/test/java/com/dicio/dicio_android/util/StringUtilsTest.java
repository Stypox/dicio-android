package com.dicio.dicio_android.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

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

        // uppercase
        assertEquals(3, StringUtils.levenshteinDistance("Kitten", "siTting"));
        assertEquals(0, StringUtils.levenshteinDistance("DOG", "dog"));
        assertEquals(0, StringUtils.levenshteinDistance("cÈd", "CèD"));

        // other characters
        assertEquals(0, StringUtils.levenshteinDistance("abc123ABC&%$", "ABC123abc&%$"));
        assertEquals(5, StringUtils.levenshteinDistance("email@email.email", "EMAILatEMAILdotEMAIL"));
    }
}
