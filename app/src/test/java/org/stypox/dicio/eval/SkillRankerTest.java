package org.stypox.dicio.eval;

import static org.dicio.skill.chain.InputRecognizer.Specificity.HIGH;
import static org.dicio.skill.chain.InputRecognizer.Specificity.LOW;
import static org.dicio.skill.chain.InputRecognizer.Specificity.MEDIUM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.dicio.skill.Skill;
import org.dicio.skill.chain.InputRecognizer;
import org.dicio.skill.util.WordExtractor;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SkillRankerTest {

    static final String input = "hi";
    static final List<String> inputWords = WordExtractor.extractWords(input);
    static final List<String> normalizedWordKeys = WordExtractor.normalizeWords(inputWords);

    private static final class TestSkill extends Skill {
        final InputRecognizer.Specificity specificity;
        final float score;
        String input;
        
        public TestSkill(final InputRecognizer.Specificity specificity, final float score) {
            this.specificity = specificity;
            this.score = score;
            this.input = null;
        }
        @Override
        public InputRecognizer.Specificity specificity() {
            return specificity;
        }
        @Override
        public float score() {
            return score;
        }


        @Override
        public void setInput(final String input,
                             final List<String> inputWords,
                             final List<String> normalizedWordKeys) {
            this.input = input;
        }
        public String getInput() {
            return input;
        }

        // useless for this test
        @Override public void processInput() {}
        @Override public void generateOutput() {}
        @Override public void cleanup() {}
    }


    private static SkillRanker getRanker(final Skill fallback,
                                             final Skill... others) {
        return new SkillRanker(Arrays.asList(others), fallback);
    }

    private static void assertRanked(final SkillRanker cr,
                                     final TestSkill fallback,
                                     final TestSkill best) {
        final Skill result =
                cr.getBest("", Collections.emptyList(), Collections.emptyList());
        final String message;
        if (result == fallback) {
            message = "Fallback skill returned by getBest";
        } else {
            message = "Skill with specificity " + result.specificity()
                    + " and score " + result.score() + " returned by getBest";
        }

        assertSame(message, best, result);
    }


    @Test
    public void testOrderPreservedAndCorrectInput() {
        TestSkill ac1 =   new TestSkill(HIGH,   0.80f),
                  ac2 =   new TestSkill(HIGH,   0.93f),
                  ac3 =   new TestSkill(HIGH,   1.00f),
                  acMed = new TestSkill(MEDIUM, 1.00f),
                  acLow = new TestSkill(LOW,    1.00f);

        final SkillRanker cr = getRanker(new TestSkill(LOW, 0.0f), ac1, acMed, ac2, acLow, ac3);
        cr.getBest(input, inputWords, normalizedWordKeys);

        assertEquals(input, ac1.getInput());
        assertEquals(input, ac2.getInput());
        assertNull(ac3.getInput());
        assertNull(acMed.getInput());
        assertNull(acLow.getInput());
    }

    @Test
    public void testHighPrHighScore() {
        final TestSkill fallback = new TestSkill(LOW, 0.0f);
        final TestSkill best = new TestSkill(HIGH, 0.92f);
        final SkillRanker cr = getRanker(fallback,
                new TestSkill(MEDIUM, 0.95f), new TestSkill(HIGH, 0.71f),
                best, new TestSkill(HIGH, 1.00f), new TestSkill(LOW, 1.0f));
        assertRanked(cr, fallback, best);
    }


    @Test
    public void testHighPrLowScore() {
        final TestSkill fallback = new TestSkill(LOW, 0.0f);
        final TestSkill best = new TestSkill(LOW, 1.0f);
        final SkillRanker cr = getRanker(fallback,
                new TestSkill(MEDIUM, 0.81f), new TestSkill(HIGH, 0.71f),
                new TestSkill(LOW, 0.85f), best, new TestSkill(HIGH, 0.32f));
        assertRanked(cr, fallback, best);
    }

    @Test
    public void testNoMatch() {
        final TestSkill fallback = new TestSkill(LOW, 0.0f);
        final SkillRanker cr = getRanker(fallback, new TestSkill(LOW, 0.8f));
        final TestSkill result = (TestSkill) cr.getBest(input, inputWords, normalizedWordKeys);
        assertNull(result); // make sure the fallback is not returned (this was once the case)
    }

    @Test
    public void testGetFallbackSkill() {
        final TestSkill fallback = new TestSkill(LOW, 0.0f);
        final SkillRanker cr = getRanker(fallback, new TestSkill(LOW, 0.8f));
        final Skill gotFallback = cr.getFallbackSkill(input, inputWords, normalizedWordKeys);
        assertSame(fallback, gotFallback);
        assertEquals(input, ((TestSkill) gotFallback).getInput());
    }
}
