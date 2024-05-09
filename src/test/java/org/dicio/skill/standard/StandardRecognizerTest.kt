package org.dicio.skill.standard

import org.dicio.skill.chain.InputRecognizer.Specificity
import org.dicio.skill.standard.word.CapturingGroup
import org.dicio.skill.standard.word.DiacriticsInsensitiveWord
import org.dicio.skill.standard.word.DiacriticsSensitiveWord
import org.dicio.skill.util.WordExtractor.extractWords
import org.dicio.skill.util.WordExtractor.normalizeWords
import org.junit.Assert
import org.junit.Test
import java.util.Collections

class StandardRecognizerTest {
    @Test
    fun testSpecificity() {
        val sr = StandardRecognizer(
            StandardRecognizerData(Specificity.HIGH)
        )
        Assert.assertEquals(Specificity.HIGH, sr.specificity())
        sr.cleanup()
    }

    @Test
    fun testCompilerReadmeMood() {
        val sr = StandardRecognizer(section_mood)
        Assert.assertEquals(Specificity.HIGH, sr.specificity())

        assertRecognized(sr, "how are you", "", 1.0f, 1.0f, emptyMap())
        assertRecognized(sr, "how are you doing", "", 1.0f, 1.0f, emptyMap())
        assertRecognized(sr, "how is it going", "", 1.0f, 1.0f, emptyMap())
        assertRecognized(sr, "how is it going over there", "has_place", 1.0f, 1.0f, emptyMap())

        assertRecognized(sr, "how is you", "", 0.4f, 0.5f, emptyMap())
        assertRecognized(sr, "hello how are you doing", "", 0.9f, 1.0f, emptyMap())
        assertRecognized(sr, "how is it", "", 0.8f, 0.9f, emptyMap())
        assertRecognized(sr, "how is it doing over there", "has_place", 0.8f, 0.9f, emptyMap())
        assertRecognized(sr, "how is it going there", "", 0.9f, 1.0f, emptyMap())
    }

    @Test
    fun testCompilerReadmeNavigation() {
        val sr = StandardRecognizer(section_GPS_navigation)
        Assert.assertEquals(Specificity.MEDIUM, sr.specificity())

        val place = Collections.singletonMap("place", "a")
        val placeAndVehicle = mapOf("place" to "a", "vehicle" to "b")

        assertRecognized(sr, "take me to a please", "question", 1.0f, 1.0f, place)
        assertRecognized(sr, "bring me to a please", "question", 1.0f, 1.0f, place)
        assertRecognized(sr, "take me to a", "question", 1.0f, 1.0f, place)
        assertRecognized(sr, "bring me to a", "question", 1.0f, 1.0f, place)
        assertRecognized(sr, "take me to a by b please", "question", 1.0f, 1.0f, placeAndVehicle)
        assertRecognized(sr, "bring me to a by b please", "question", 1.0f, 1.0f, placeAndVehicle)
        assertRecognized(sr, "take me to a by b", "question", 1.0f, 1.0f, placeAndVehicle)
        assertRecognized(sr, "bring me to a by b", "question", 1.0f, 1.0f, placeAndVehicle)
        assertRecognized(sr, "give me directions to a please", "question", 1.0f, 1.0f, place)
        assertRecognized(sr, "give me directions to a", "question", 1.0f, 1.0f, place)
        assertRecognized(sr, "how do i get to a", "question", 1.0f, 1.0f, place)
        assertRecognized(sr, "how can i get to a", "question", 1.0f, 1.0f, place)
        assertRecognized(sr, "i want to go to a", "statement", 1.0f, 1.0f, place)
        assertRecognized(sr, "i want to go to a by b", "statement", 1.0f, 1.0f, placeAndVehicle)
        assertRecognized(sr, "a is the place i want to go to", "statement", 1.0f, 1.0f, place)

        assertRecognized(sr, "hey take me to a please", "question", 0.9f, 1.0f, place)
        assertRecognized(sr, "hello car bring me to a please", "question", 0.7f, 0.8f, place)
        assertRecognized(sr, "take you to a by b please", "question", 0.8f, 0.9f, placeAndVehicle)
        assertRecognized(sr, "gave me directions to a", "question", 0.7f, 0.8f, place)
        assertRecognized(sr, "please i want to go to a", "statement", 0.9f, 1.0f, place)
        assertRecognized(
            sr,
            "please i want to go to a by b",
            "statement",
            0.9f,
            1.0f,
            placeAndVehicle
        )
    }

    @Test
    fun testSmallerCapturingGroupIsPreferred() {
        val sr = StandardRecognizer(section_hello)
        Assert.assertEquals(Specificity.LOW, sr.specificity())

        val guys = Collections.singletonMap("hi", "guys")
        val girlsAndBoys = Collections.singletonMap("hi", "girls and boys")
        val hello = Collections.singletonMap("hi", "hello")

        assertRecognized(sr, "hello guys", "first", 1.0f, 1.0f, guys)
        assertRecognized(sr, "hello girls and boys", "first", 1.0f, 1.0f, girlsAndBoys)
        assertRecognized(sr, "hello hello guys", "second", 1.0f, 1.0f, guys)
        assertRecognized(sr, "hello hello girls and boys", "second", 1.0f, 1.0f, girlsAndBoys)
        assertRecognized(sr, "hello hello", "first", 1.0f, 1.0f, hello)
        assertRecognized(sr, "hello hello hello", "second", 1.0f, 1.0f, hello)
    }

    companion object {
        val section_mood: StandardRecognizerData = StandardRecognizerData(
            Specificity.HIGH,
            Sentence(
                "",
                intArrayOf(0),
                DiacriticsSensitiveWord("how", 4, 1, 4),
                DiacriticsSensitiveWord("are", 3, 2),
                DiacriticsSensitiveWord("you", 2, 3, 7),
                DiacriticsSensitiveWord("doing", 1, 7),
                DiacriticsSensitiveWord("is", 3, 5),
                DiacriticsSensitiveWord("it", 2, 6),
                DiacriticsSensitiveWord("going", 1, 7)
            ),
            Sentence(
                "has_place",
                intArrayOf(0),
                DiacriticsSensitiveWord("how", 6, 1),
                DiacriticsSensitiveWord("is", 5, 2),
                DiacriticsSensitiveWord("it", 4, 3),
                DiacriticsSensitiveWord("going", 3, 4),
                DiacriticsSensitiveWord("over", 2, 5),
                DiacriticsSensitiveWord("there", 1, 6)
            )
        )

        val section_GPS_navigation: StandardRecognizerData = StandardRecognizerData(
            Specificity.MEDIUM,
            Sentence(
                "question",
                intArrayOf(0, 1),
                DiacriticsSensitiveWord("take", 9, 2),
                DiacriticsSensitiveWord("bring", 11, 2),
                DiacriticsSensitiveWord("me", 10, 3),
                DiacriticsSensitiveWord("to", 9, 4),
                CapturingGroup("place", 8, 5, 7, 8),
                DiacriticsSensitiveWord("by", 6, 6),
                CapturingGroup("vehicle", 5, 7, 8),
                DiacriticsSensitiveWord("please", 4, 8)
            ),
            Sentence(
                "question",
                intArrayOf(0),
                DiacriticsSensitiveWord("give", 7, 1),
                DiacriticsSensitiveWord("me", 6, 2),
                DiacriticsSensitiveWord("directions", 5, 3),
                DiacriticsSensitiveWord("to", 4, 4),
                CapturingGroup("place", 3, 5, 6),
                DiacriticsSensitiveWord("please", 1, 6)
            ),
            Sentence(
                "question",
                intArrayOf(0),
                DiacriticsSensitiveWord("how", 9, 1, 2),
                DiacriticsSensitiveWord("do", 6, 3),
                DiacriticsSensitiveWord("can", 8, 3),
                DiacriticsSensitiveWord("i", 7, 4),
                DiacriticsSensitiveWord("get", 6, 5),
                DiacriticsSensitiveWord("to", 5, 6),
                CapturingGroup("place", 4, 7)
            ),
            Sentence(
                "statement",
                intArrayOf(0),
                DiacriticsSensitiveWord("i", 10, 1),
                DiacriticsSensitiveWord("want", 9, 2),
                DiacriticsSensitiveWord("to", 8, 3),
                DiacriticsSensitiveWord("go", 7, 4),
                DiacriticsSensitiveWord("to", 6, 5),
                CapturingGroup("place", 5, 6, 8),
                DiacriticsSensitiveWord("by", 3, 7),
                CapturingGroup("vehicle", 2, 8)
            ),
            Sentence(
                "statement",
                intArrayOf(0),
                CapturingGroup("place", 10, 1),
                DiacriticsSensitiveWord("is", 8, 2),
                DiacriticsSensitiveWord("the", 7, 3),
                DiacriticsSensitiveWord("place", 6, 4),
                DiacriticsSensitiveWord("i", 5, 5),
                DiacriticsSensitiveWord("want", 4, 6),
                DiacriticsSensitiveWord("to", 3, 7),
                DiacriticsSensitiveWord("go", 2, 8),
                DiacriticsSensitiveWord("to", 1, 9)
            )
        )

        val section_hello: StandardRecognizerData = StandardRecognizerData(
            Specificity.LOW,
            Sentence(
                "first", intArrayOf(0),
                DiacriticsInsensitiveWord("hello", 3, 1), CapturingGroup("hi", 2, 2)
            ),
            Sentence(
                "second",
                intArrayOf(0),
                DiacriticsInsensitiveWord("hello", 4, 1),
                DiacriticsInsensitiveWord("hello", 3, 2),
                CapturingGroup("hi", 2, 3)
            )
        )


        private fun assertRecognized(
            sr: StandardRecognizer, input: String,
            sentenceId: String,
            a: Float, b: Float,
            capturingGroups: Map<String, String>
        ) {
            val inputWords = extractWords(input)
            val normalizedInputWords = normalizeWords(inputWords)
            sr.setInput(input, inputWords, normalizedInputWords)
            val score = sr.score()
            val result = sr.result
            sr.cleanup()
            Assert.assertEquals(sentenceId, result.sentenceId)

            if (a == b) {
                Assert.assertEquals(
                    "Score $score is not equal to $a",
                    a, score, SentenceTest.FLOAT_EQUALS_DELTA
                )
            } else {
                Assert.assertTrue(
                    "Score $score is not in range [$a, $b]",
                    score in a..b
                )
            }

            Assert.assertEquals(
                capturingGroups.size.toLong(),
                result.capturingGroupRanges.size.toLong()
            )
            for ((key, value) in capturingGroups) {
                SentenceTest.assertCapturingGroup(
                    inputWords,
                    result.capturingGroupRanges[key],
                    value
                )
            }
        }
    }
}
