package org.dicio.skill.standard;

import org.dicio.skill.standard.word.BaseWord;
import org.dicio.skill.standard.word.CapturingGroup;
import org.dicio.skill.standard.word.DiacriticsInsensitiveWord;
import org.dicio.skill.standard.word.DiacriticsSensitiveWord;
import org.dicio.skill.util.WordExtractor;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SentenceTest {
    // [1-3]p means [1-3] packs, that is, [0-2] capturing groups (e.g. test1p=testOnePack)

    static final float floatEqualsDelta = 0.0001f;


    private static DiacriticsSensitiveWord dsw(final String value,
                                               final int minimumSkippedWordsToEnd,
                                               final int... nextIndices) {
        return new DiacriticsSensitiveWord(value, minimumSkippedWordsToEnd, nextIndices);
    }

    private static DiacriticsInsensitiveWord diw(final String value,
                                                 final int minimumSkippedWordsToEnd,
                                                 final int... nextIndices) {
        return new DiacriticsInsensitiveWord(WordExtractor.nfkdNormalizeWord(value),
                minimumSkippedWordsToEnd, nextIndices);
    }

    private static CapturingGroup capt(final String name,
                                       final int minimumSkippedWordsToEnd,
                                       final int... nextIndices) {
        return new CapturingGroup(name, minimumSkippedWordsToEnd, nextIndices);
    }

    private static void addAllWords(final List<String> packWords,
                                    final List<BaseWord> words,
                                    final int minimumSkippedWordsToEnd) {
        for (int i = 0; i < packWords.size(); ++i) {
            words.add(dsw(packWords.get(i),
                    minimumSkippedWordsToEnd + packWords.size() - i, words.size() + 1));
        }
    }

    private static void addCapturingGroup(int index,
                                          final List<BaseWord> words,
                                          final int minimumSkippedWordsToEnd) {
        words.add(capt(Integer.toString(index),
                minimumSkippedWordsToEnd + 2, words.size() + 1));
    }


    private static Sentence sent(final String pack1) {
        final List<BaseWord> words = new ArrayList<>();
        final List<String> pack1Words = WordExtractor.extractWords(pack1);

        addAllWords(pack1Words, words, 0);
        return new Sentence("", new int[] {0}, words.toArray(new BaseWord[0]));
    }

    private static Sentence sent(final String pack1, final String pack2) {
        final List<BaseWord> words = new ArrayList<>();
        final List<String> pack1Words = WordExtractor.extractWords(pack1);
        final List<String> pack2Words = WordExtractor.extractWords(pack2);

        addAllWords(pack1Words, words, 2 + pack2Words.size());
        addCapturingGroup(0, words, pack2Words.size());
        addAllWords(pack2Words, words, 0);
        return new Sentence("", new int[] {0}, words.toArray(new BaseWord[0]));
    }

    private static Sentence sent(final String pack1, final String pack2, final String pack3) {
        final List<BaseWord> words = new ArrayList<>();
        final List<String> pack1Words = WordExtractor.extractWords(pack1);
        final List<String> pack2Words = WordExtractor.extractWords(pack2);
        final List<String> pack3Words = WordExtractor.extractWords(pack3);

        addAllWords(pack1Words, words, 4 + pack2Words.size() + pack3Words.size());
        addCapturingGroup(0, words, 2 + pack2Words.size() + pack3Words.size());
        addAllWords(pack2Words, words, 2 + pack3Words.size());
        addCapturingGroup(1, words, pack3Words.size());
        addAllWords(pack3Words, words, 0);
        return new Sentence("", new int[] {0}, words.toArray(new BaseWord[0]));
    }


    static void assertCapturingGroup(final List<String> inputWords,
                                     final InputWordRange range,
                                     final String captGr) {
        if (captGr == null) {
            assertNull(range);
            return;
        }

        final List<String> captGrWords = WordExtractor.extractWords(captGr);
        final List<String> actualCaptGrWords = new ArrayList<>();
        for (int i = range.from(); i < range.to(); ++i) {
            actualCaptGrWords.add(inputWords.get(i));
        }

        assertThat(actualCaptGrWords, CoreMatchers.is(captGrWords));
    }
    
    private static void assertSentence(final Sentence s, final String input,
                                       final float a, final float b,
                                       final String captGr0, final String captGr1) {
        final List<String> inputWords = WordExtractor.extractWords(input);
        final List<String> normalizedInputWords = WordExtractor.normalizeWords(inputWords);
        final PartialScoreResult scoreResult = s.score(inputWords, normalizedInputWords);
        final float score = scoreResult.value(inputWords.size());

        if (a == b) {
            assertEquals("Score " + score + " " + scoreResult + " is not equal to " + a,
                    a, score, floatEqualsDelta);
        } else {
            assertTrue("Score " + score + " " + scoreResult + " is not in range [" + a + ", " + b + "]",
                    a <= score && score <= b);
        }

        final StandardResult r = scoreResult.toStandardResult(s.getSentenceId(), input);
        assertEquals((captGr0 != null ? 1 : 0) + (captGr1 != null ? 1 : 0),
                r.getCapturingGroupRanges().size());
        assertCapturingGroup(inputWords, r.getCapturingGroupRanges().get("0"), captGr0);
        assertCapturingGroup(inputWords, r.getCapturingGroupRanges().get("1"), captGr1);
    }


    @Test
    public void test1p() {
        final Sentence s = sent("hello how are you");

        assertSentence(s, "hello how are you",     1.0f, 1.0f, null, null);
        assertSentence(s, "hello how is you",      0.7f, 0.8f, null, null);
        assertSentence(s, "hello how are you bob", 0.9f, 1.0f, null, null);
        assertSentence(s, "mary",                  0.0f, 0.0f, null, null);
        assertSentence(s, "",                      0.0f, 0.0f, null, null);
    }

    @Test
    public void test2p() {
        final Sentence s = sent("hello", "how are you");

        assertSentence(s, "hello bob how are you",                     1.0f, 1.0f, "bob",             null);
        assertSentence(s, "hello bob and mary how is you",             0.7f, 0.8f, "bob and mary",    null);
        assertSentence(s, "hello mary how are steaks inside you",      0.7f, 0.8f, "mary",            null);
        assertSentence(s, "hello bob how are steaks doing inside you", 0.5f, 0.6f, "bob",             null);
        assertSentence(s, "hi hello bob how are not you",              0.7f, 0.8f, "bob" ,            null);
        assertSentence(s, "hi hello mary and bob how are not you",     0.7f, 0.8f, "mary and bob",    null);
        assertSentence(s, "hello mary",                                0.1f, 0.2f, "mary",            null);
        assertSentence(s, "bob how are you",                           0.8f, 0.9f, "bob",             null);
        assertSentence(s, "mary and bob",                              0.0f, 0.1f, "mary and bob",    null);
        assertSentence(s, "hello how are you",                         0.8f, 0.9f, "how",             null);
        assertSentence(s, "",                                          0.0f, 0.0f, null,              null);
        assertSentence(s, "a a a a hello b how are you",               0.8f, 0.9f, "a a a a hello b", null);
        assertSentence(s, "hello b how a a a a are you",               0.8f, 0.9f, "b how a a a a",   null);
    }

    @Test
    public void test2pLeftEmpty() {
        final Sentence s = sent("", "how are you");

        assertSentence(s, "hello bob how are you",                     1.0f, 1.0f, "hello bob",          null);
        assertSentence(s, "hello bob and mary how is you",             0.6f, 0.7f, "hello bob and mary", null);
        assertSentence(s, "hello mary how are steaks inside you",      0.6f, 0.7f, "hello mary",         null);
        assertSentence(s, "hello bob how are steaks doing inside you", 0.3f, 0.4f, "hello bob",          null);
        assertSentence(s, "hi hello bob how are not you",              0.8f, 0.9f, "hi hello bob" ,      null);
        assertSentence(s, "bob how are you",                           1.0f, 1.0f, "bob",                null);
        assertSentence(s, "mary and bob",                              0.0f, 0.1f, "mary and bob",       null);
        assertSentence(s, "how are you",                               0.8f, 0.9f, "how",                null);
        assertSentence(s, "",                                          0.0f, 0.0f, null,                 null);
    }

    @Test
    public void test2pRightEmpty() {
        final Sentence s = sent("hello", "");

        assertSentence(s, "hello bob",       1.0f, 1.0f, "bob",             null);
        assertSentence(s, "hi hello bob",    0.3f, 0.4f, "bob",             null);
        assertSentence(s, "hi hi hello bob", 0.2f, 0.3f, "hi hi hello bob", null);
        assertSentence(s, "mary and bob",    0.2f, 0.3f, "mary and bob",    null);
        assertSentence(s, "hello",           0.1f, 0.2f, null,              null);
        assertSentence(s, "",                0.0f, 0.0f, null,              null);
    }


    @Test
    public void test3p() {
        final Sentence s = sent("i want", "liters of", "please");

        assertSentence(s, "i want five liters of milk please",                1.0f, 1.0f, "five",            "milk");
        assertSentence(s, "i want five and a half liters of soy milk please", 1.0f, 1.0f, "five and a half", "soy milk");
        assertSentence(s, "i want five liters of milk",                       0.9f, 1.0f, "five",            "milk");
        assertSentence(s, "five and a half liters of soy milk",               0.3f, 0.4f, "five and a half", "soy milk");
        assertSentence(s, "i want one liter of milk please",                  0.9f, 1.0f, "one liter",       "milk");
        assertSentence(s, "i want one liter milk please",                     0.6f, 0.7f, "one",             "liter milk");
        assertSentence(s, "i want one liter soy milk please",                 0.6f, 0.7f, "one",             "liter soy milk");
        assertSentence(s, "i want one liter of milk",                         0.6f, 0.7f, "one liter",       "milk");
        assertSentence(s, "one liter of soy milk",                            0.1f, 0.2f, "one liter",       "soy milk");
        assertSentence(s, "i want milk please",                               0.3f, 0.4f, "milk",            "please");
        assertSentence(s, "i want please",                                    0.1f, 0.2f, "please",          null);
        assertSentence(s, "i do want please",                                 0.3f, 0.4f, "do",              "want");
        assertSentence(s, "i want",                                           0.0f, 0.1f, null,              null);
        assertSentence(s, "you want five liters of milk please",              0.8f, 0.9f, "five",            "milk");
        assertSentence(s, "i need five liters of milk please",                0.9f, 1.0f, "need five",       "milk");
        assertSentence(s, "you need five liters of milk please",              0.6f, 0.7f, "you need five",   "milk");
        assertSentence(s, "i want five liters of soy milk",                   0.9f, 1.0f, "five",            "soy milk");
        assertSentence(s, "i need five liters of soy milk",                   0.6f, 0.7f, "need five",       "soy milk");
        assertSentence(s, "one soy milk",                                     0.0f, 0.1f, "one",             "soy milk");
        assertSentence(s, "milk",                                             0.0f, 0.1f, "milk",            null);
        assertSentence(s, "",                                                 0.0f, 0.0f, null,              null);
        assertSentence(s, "i a a a a want f liters of milk please",           0.9f, 1.0f, "a a a a want f",  "milk");
        assertSentence(s, "i want five liters of m please a a a a",           0.5f, 0.6f, "five",            "m");
    }

    @Test
    public void test3pLeftEmpty() {
        final Sentence s = sent("", "liters of", "please");

        assertSentence(s, "five liters of milk please",                1.0f, 1.0f, "five",            "milk");
        assertSentence(s, "five and a half liters of soy milk please", 1.0f, 1.0f, "five and a half", "soy milk");
        assertSentence(s, "five liters of milk",                       0.8f, 0.9f, "five",            "milk");
        assertSentence(s, "one liter of milk please",                  0.8f, 0.9f, "one liter",       "milk");
        assertSentence(s, "one liter soy milk please",                 0.3f, 0.4f, "one",             "liter soy milk");
        assertSentence(s, "one liter of milk",                         0.3f, 0.4f, "one liter",       "milk");
        assertSentence(s, "milk please",                               0.1f, 0.2f, "milk",            "please");
        assertSentence(s, "please",                                    0.0f, 0.1f, "please",          null);
        assertSentence(s, "one soy milk",                              0.1f, 0.2f, "one",             "soy milk");
        assertSentence(s, "milk",                                      0.0f, 0.1f, "milk",            null);
        assertSentence(s, "",                                          0.0f, 0.0f, null,              null);
    }

    @Test
    public void test3pRightEmpty() {
        final Sentence s = sent("i want", "liters of", "");

        assertSentence(s, "i want five liters of milk",                1.0f, 1.0f, "five",            "milk");
        assertSentence(s, "i want five and a half liters of soy milk", 1.0f, 1.0f, "five and a half", "soy milk");
        assertSentence(s, "five and a half liters of soy milk",        0.5f, 0.6f, "five and a half", "soy milk");
        assertSentence(s, "i want one liter of milk",                  0.9f, 1.0f, "one liter",       "milk");
        assertSentence(s, "i want one liter milk",                     0.5f, 0.6f, "one",             "liter milk");
        assertSentence(s, "one liter of soy milk",                     0.2f, 0.3f, "one liter",       "soy milk");
        assertSentence(s, "i want milk",                               0.1f, 0.2f, "milk",            null);
        assertSentence(s, "i want",                                    0.0f, 0.1f, null,              null);
        assertSentence(s, "you want five liters of milk",              0.8f, 0.9f, "five",            "milk");
        assertSentence(s, "i need five liters of milk",                0.9f, 1.0f, "need five",       "milk");
        assertSentence(s, "you need five liters of milk",              0.5f, 0.6f, "you need five",   "milk");
        assertSentence(s, "one soy milk",                              0.1f, 0.2f, "one",             "soy milk");
        assertSentence(s, "milk",                                      0.0f, 0.1f, "milk",            null);
        assertSentence(s, "",                                          0.0f, 0.0f, null,              null);
    }

    @Test
    public void test3pLeftRightEmpty() {
        final Sentence s = sent("", "and", "");

        assertSentence(s, "bob and mary",           1.0f, 1.0f, "bob",          "mary");
        assertSentence(s, "bob and mary and simon", 1.0f, 1.0f, "bob",          "mary and simon");
        assertSentence(s, "bob mary",               0.5f, 0.6f, "bob",          "mary");
        assertSentence(s, "and mary",               0.5f, 0.6f, "and",          "mary");
        assertSentence(s, "bob and",                0.2f, 0.3f, "bob",          null);
        assertSentence(s, "",                       0.0f, 0.0f, null,           null);
    }


    @Test
    public void testDuplicateWord() {
        final Sentence s = sent("how do you do bob");

        assertSentence(s, "how do you do bob",     1.0f, 1.0f, null, null);
        assertSentence(s, "how does you do bob",   0.8f, 0.9f, null, null);
        assertSentence(s, "how does a you do bob", 0.6f, 0.7f, null, null);
    }

    @Test
    public void testOptionalFollowedByCapturingGroup() {
        final Sentence s = new Sentence("", new int[] {0},
                diw("open",        1, 1, 3),
                diw("the",         2, 2),
                dsw("application", 1, 3),
                capt("0",          0, 4));

        assertSentence(s, "open newpipe",                 1.0f, 1.0f, "newpipe", null);
        assertSentence(s, "open the application newpipe", 1.0f, 1.0f, "newpipe", null);
        assertSentence(s, "open the newest newpipe",      1.0f, 1.0f, "the newest newpipe", null);
    }

    @Test
    public void testCapturingGroupFollowedByOptional() {
        final Sentence s = new Sentence("", new int[] {0},
                dsw("buy",    1, 1),
                capt("0",     0, 2, 3),
                diw("please", 0, 3));

        assertSentence(s, "buy please",     1.0f, 1.0f, "please",   null);
        assertSentence(s, "buy soy please", 1.0f, 1.0f, "soy",      null);
        assertSentence(s, "buy soy milk",   1.0f, 1.0f, "soy milk", null);
    }

    @Test
    public void testOptionalCapturingGroup() {
        final Sentence s = new Sentence("", new int[] {0},
                diw("weather", 1, 1, 2),
                capt("0",      0, 2));

        assertSentence(s, "weather",          1.0f, 1.0f, null,       null);
        assertSentence(s, "weather new",      1.0f, 1.0f, "new",      null);
        assertSentence(s, "weather new york", 1.0f, 1.0f, "new york", null);
    }
}