package org.dicio.skill.standard;

import org.dicio.skill.InputRecognizer;
import org.dicio.skill.standard.word.CapturingGroup;
import org.dicio.skill.standard.word.DiacriticsSensitiveWord;
import org.dicio.skill.util.WordExtractor;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StandardRecognizerTest {

    public static final StandardRecognizerData section_mood = new StandardRecognizerData(
            InputRecognizer.Specificity.high,
            new Sentence("", new int[] {0,},
                    new DiacriticsSensitiveWord("how", 4, 1, 4), new DiacriticsSensitiveWord("are", 3, 2), new DiacriticsSensitiveWord("you", 2, 3, 7),
                    new DiacriticsSensitiveWord("doing", 1, 7), new DiacriticsSensitiveWord("is", 3, 5), new DiacriticsSensitiveWord("it", 2, 6),
                    new DiacriticsSensitiveWord("going", 1, 7)),
            new Sentence("has_place", new int[] {0,},
                    new DiacriticsSensitiveWord("how", 6, 1), new DiacriticsSensitiveWord("is", 5, 2), new DiacriticsSensitiveWord("it", 4, 3),
                    new DiacriticsSensitiveWord("going", 3, 4), new DiacriticsSensitiveWord("over", 2, 5), new DiacriticsSensitiveWord("there", 1, 6)));

    public static final StandardRecognizerData section_GPS_navigation = new StandardRecognizerData(
            InputRecognizer.Specificity.medium,
            new Sentence("question", new int[] {0, 1,},
                    new DiacriticsSensitiveWord("take", 9, 2), new DiacriticsSensitiveWord("bring", 11, 2), new DiacriticsSensitiveWord("me", 10, 3),
                    new DiacriticsSensitiveWord("to", 9, 4), new CapturingGroup("place", 8, 5, 7, 8), new DiacriticsSensitiveWord("by", 6, 6),
                    new CapturingGroup("vehicle", 5, 7, 8), new DiacriticsSensitiveWord("please", 4, 8)),
            new Sentence("question", new int[] {0,},
                    new DiacriticsSensitiveWord("give", 7, 1), new DiacriticsSensitiveWord("me", 6, 2), new DiacriticsSensitiveWord("directions", 5, 3),
                    new DiacriticsSensitiveWord("to", 4, 4), new CapturingGroup("place", 3, 5, 6), new DiacriticsSensitiveWord("please", 1, 6)),
            new Sentence("question", new int[] {0,},
                    new DiacriticsSensitiveWord("how", 9, 1, 2), new DiacriticsSensitiveWord("do", 6, 3), new DiacriticsSensitiveWord("can", 8, 3),
                    new DiacriticsSensitiveWord("i", 7, 4), new DiacriticsSensitiveWord("get", 6, 5), new DiacriticsSensitiveWord("to", 5, 6),
                    new CapturingGroup("place", 4, 7)),
            new Sentence("statement", new int[] {0,},
                    new DiacriticsSensitiveWord("i", 10, 1), new DiacriticsSensitiveWord("want", 9, 2), new DiacriticsSensitiveWord("to", 8, 3),
                    new DiacriticsSensitiveWord("go", 7, 4), new DiacriticsSensitiveWord("to", 6, 5), new CapturingGroup("place", 5, 6, 8),
                    new DiacriticsSensitiveWord("by", 3, 7), new CapturingGroup("vehicle", 2, 8)),
            new Sentence("statement", new int[] {0,},
                    new CapturingGroup("place", 10, 1), new DiacriticsSensitiveWord("is", 8, 2), new DiacriticsSensitiveWord("the", 7, 3),
                    new DiacriticsSensitiveWord("place", 6, 4), new DiacriticsSensitiveWord("i", 5, 5), new DiacriticsSensitiveWord("want", 4, 6),
                    new DiacriticsSensitiveWord("to", 3, 7), new DiacriticsSensitiveWord("go", 2, 8), new DiacriticsSensitiveWord("to", 1, 9)));


    private static void assertRecognized(final StandardRecognizer sr, final String input,
                                         final String sentenceId,
                                         final float a, final float b,
                                         final Map<String, String> capturingGroups) {
        final List<String> inputWords = WordExtractor.extractWords(input);
        final List<String> normalizedInputWords = WordExtractor.normalizeWords(inputWords);
        sr.setInput(input, inputWords, normalizedInputWords);
        final float score = sr.score();
        final StandardResult result = sr.getResult();
        sr.cleanup();
        assertEquals(sentenceId, result.getSentenceId());

        if (a == b) {
            assertEquals("Score " + score + " is not equal to " + a,
                    a, score, SentenceTest.floatEqualsDelta);
        } else {
            assertTrue("Score " + score + " is not in range [" + a + ", " + b + "]",
                    a <= score && score <= b);
        }

        assertEquals(capturingGroups.size(), result.getCapturingGroupRanges().size());
        for (final Map.Entry<String, String> capturingGroup : capturingGroups.entrySet()) {
            SentenceTest.assertCapturingGroup(inputWords,
                    result.getCapturingGroupRanges().get(capturingGroup.getKey()),
                    capturingGroup.getValue());
        }
    }


    @Test
    public void testSpecificity() {
        final StandardRecognizer sr = new StandardRecognizer(
                new StandardRecognizerData(InputRecognizer.Specificity.high));
        assertEquals(InputRecognizer.Specificity.high, sr.specificity());
        sr.cleanup();
    }

    @Test
    public void testCompilerReadmeMood() {
        final StandardRecognizer sr = new StandardRecognizer(section_mood);
        assertEquals(InputRecognizer.Specificity.high, sr.specificity());

        assertRecognized(sr, "how are you",                "",          1.0f, 1.0f, Collections.emptyMap());
        assertRecognized(sr, "how are you doing",          "",          1.0f, 1.0f, Collections.emptyMap());
        assertRecognized(sr, "how is it going",            "",          1.0f, 1.0f, Collections.emptyMap());
        assertRecognized(sr, "how is it going over there", "has_place", 1.0f, 1.0f, Collections.emptyMap());

        assertRecognized(sr, "how is you",                 "",          0.4f, 0.5f, Collections.emptyMap());
        assertRecognized(sr, "hello how are you doing",    "",          0.9f, 1.0f, Collections.emptyMap());
        assertRecognized(sr, "how is it",                  "",          0.8f, 0.9f, Collections.emptyMap());
        assertRecognized(sr, "how is it doing over there", "has_place", 0.8f, 0.9f, Collections.emptyMap());
        assertRecognized(sr, "how is it going there",      "",          0.9f, 1.0f, Collections.emptyMap());
    }

    @Test
    public void testCompilerReadmeNavigation() {
        final StandardRecognizer sr = new StandardRecognizer(section_GPS_navigation);
        assertEquals(InputRecognizer.Specificity.medium, sr.specificity());

        final Map<String, String> place = Collections.singletonMap("place", "a");
        final Map<String, String> placeAndVehicle = new HashMap<String, String>() {{
            put("place", "a"); put("vehicle", "b"); }};

        assertRecognized(sr, "take me to a please",            "question",  1.0f, 1.0f, place);
        assertRecognized(sr, "bring me to a please",           "question",  1.0f, 1.0f, place);
        assertRecognized(sr, "take me to a",                   "question",  1.0f, 1.0f, place);
        assertRecognized(sr, "bring me to a",                  "question",  1.0f, 1.0f, place);
        assertRecognized(sr, "take me to a by b please",       "question",  1.0f, 1.0f, placeAndVehicle);
        assertRecognized(sr, "bring me to a by b please",      "question",  1.0f, 1.0f, placeAndVehicle);
        assertRecognized(sr, "take me to a by b",              "question",  1.0f, 1.0f, placeAndVehicle);
        assertRecognized(sr, "bring me to a by b",             "question",  1.0f, 1.0f, placeAndVehicle);
        assertRecognized(sr, "give me directions to a please", "question",  1.0f, 1.0f, place);
        assertRecognized(sr, "give me directions to a",        "question",  1.0f, 1.0f, place);
        assertRecognized(sr, "how do i get to a",              "question",  1.0f, 1.0f, place);
        assertRecognized(sr, "how can i get to a",             "question",  1.0f, 1.0f, place);
        assertRecognized(sr, "i want to go to a",              "statement", 1.0f, 1.0f, place);
        assertRecognized(sr, "i want to go to a by b",         "statement", 1.0f, 1.0f, placeAndVehicle);
        assertRecognized(sr, "a is the place i want to go to", "statement", 1.0f, 1.0f, place);

        assertRecognized(sr, "hey take me to a please",        "question",  0.9f, 1.0f, place);
        assertRecognized(sr, "hello car bring me to a please", "question",  0.7f, 0.8f, place);
        assertRecognized(sr, "take you to a by b please",      "question",  0.8f, 0.9f, placeAndVehicle);
        assertRecognized(sr, "gave me directions to a",        "question",  0.7f, 0.8f, place);
        assertRecognized(sr, "please i want to go to a",       "statement", 0.9f, 1.0f, place);
        assertRecognized(sr, "please i want to go to a by b",  "statement", 0.9f, 1.0f, placeAndVehicle);
    }
}