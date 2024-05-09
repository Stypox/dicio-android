package org.stypox.dicio.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.stypox.dicio.util.StringUtils.contactStringDistance
import org.stypox.dicio.util.StringUtils.customStringDistance
import org.stypox.dicio.util.StringUtils.join
import org.stypox.dicio.util.StringUtils.levenshteinDistance
import org.stypox.dicio.util.StringUtils.removePunctuation

class StringUtilsTest {
    @Test
    fun defaultJoinTest() {
        assertEquals("a b c", join(mutableListOf("a", "b", "c")))
        assertEquals("", join(emptyList()))
        assertEquals("abc", join(listOf("abc")))
        assertEquals(" ", join(mutableListOf("", "")))
    }

    @Test
    fun joinTest() {
        assertEquals("a-b-c", join(mutableListOf("a", "b", "c"), "-"))
        assertEquals("", join(emptyList(), "-"))
        assertEquals("abc", join(listOf("abc"), "-"))
        assertEquals("-", join(mutableListOf("", ""), "-"))
    }

    @Test
    fun removePunctuationTest() {
        assertEquals("hello how are you ", removePunctuation("hello, how are you? "))
        assertEquals("12345", removePunctuation("!\"#1$%&'()*+2,-./:;<=34>?@[5]^_`{|}~"))
    }

    @Test
    fun levenshteinDistanceTest() {
        assertEquals(0, levenshteinDistance("kitten", "kitten").toLong())
        assertEquals(1, levenshteinDistance("kitten", "sitten").toLong())
        assertEquals(2, levenshteinDistance("kitten", "sittin").toLong())
        assertEquals(3, levenshteinDistance("kitten", "sitting").toLong())
        assertEquals(0, levenshteinDistance("dog", "dog").toLong())
        assertEquals(4, levenshteinDistance("dòg", "caty").toLong())
        assertEquals(2, levenshteinDistance("dog", "doggy").toLong())
        assertEquals(2, levenshteinDistance("dog", "dosgy").toLong())
        assertEquals(3, levenshteinDistance("dog", "dosay").toLong())
        assertEquals(1, levenshteinDistance("dog", "doag").toLong())
        assertEquals(1, levenshteinDistance("dog", "dogè").toLong())
    }

    @Test
    fun levenshteinDistanceCaseTest() {
        assertEquals(3, levenshteinDistance("Kitten", "siTting").toLong())
        assertEquals(0, levenshteinDistance("DOG", "dog").toLong())
        assertEquals(0, levenshteinDistance("cÈd", "CèD").toLong())
    }

    @Test
    fun levenshteinDistanceSpecialTest() {
        assertEquals(3, levenshteinDistance("abc123ABC&%$", "&%\$ABCabc").toLong())
        assertEquals(0, levenshteinDistance("abc123ABC&%$", "ABC&123%abc.").toLong())
        assertEquals(
            5,
            levenshteinDistance("email@email.email", "EMAIL#atEMAIL#dotEMAIL").toLong()
        )
        assertEquals(
            0,
            levenshteinDistance("Hello, hòw are you?", "hellohoware!you").toLong()
        )
    }

    @Test
    fun customStringDistanceTest() {
        assertEquals(-12, customStringDistance("kitten", "kitten").toLong())
        assertEquals(-9, customStringDistance("kitten", "sitten").toLong())
        assertEquals(-5, customStringDistance("kitten", "sittin").toLong())
        assertEquals(-5, customStringDistance("kitten", "sitlen").toLong())
        assertEquals(-4, customStringDistance("kitten", "sitting").toLong())
        assertEquals(-6, customStringDistance("kitten", "sittieng").toLong())
        assertEquals(-6, customStringDistance("dog", "dog").toLong())
        assertEquals(4, customStringDistance("dòg", "caty").toLong())
        assertEquals(-3, customStringDistance("dog", "dosgy").toLong())
        assertEquals(-1, customStringDistance("dog", "dosay").toLong())
        assertEquals(-4, customStringDistance("dog", "doag").toLong())
        assertEquals(-5, customStringDistance("dog", "dogè").toLong())
    }

    @Test
    fun customStringDistanceCaseTest() {
        assertEquals(-4, customStringDistance("Kitten", "siTting").toLong())
        assertEquals(-6, customStringDistance("DOG", "dog").toLong())
        assertEquals(-6, customStringDistance("cÈd", "CèD").toLong())
    }

    @Test
    fun customStringDistanceSpecialTest() {
        assertEquals(-6, customStringDistance("abc123ABC&%$", "&%\$ABCabc").toLong())
        assertEquals(-18, customStringDistance("abc123ABC&%$", "ABC&123%abc.").toLong())
        assertEquals(
            -20,
            customStringDistance("email@email.email", "EMAIL#atEMAIL#dotEMAIL").toLong()
        )
        assertEquals(
            -28,
            customStringDistance("Hello, hòw are you?", "hellohoware!you").toLong()
        )
    }

    @Test
    fun customStringDistanceRepeatedLettersTest() {
        assertEquals(-5, customStringDistance("hello", "Helloo Hiii").toLong())
        assertEquals(-6, customStringDistance("hello", "Hello Guys").toLong())
        assertEquals(-6, customStringDistance("hello", "Hello 2uys").toLong())
        assertEquals(-5, customStringDistance("dog", "dogy").toLong())
        assertEquals(-4, customStringDistance("dog", "doggy").toLong())
    }

    @Test
    fun contactStringDistanceTest() {
        assertEquals(-12, contactStringDistance("Leo Morgan", "Morgan").toLong())
        assertEquals(-12, contactStringDistance("John Morgan", "Morgan").toLong())
        assertEquals(-7, contactStringDistance("Johan Morgan", "John").toLong())
        assertEquals(-4, contactStringDistance("Leonard John", "Morgan").toLong())
    }
}
