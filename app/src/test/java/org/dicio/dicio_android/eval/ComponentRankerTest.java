package org.dicio.dicio_android.eval;

import android.content.Context;

import org.dicio.component.InputRecognizer;
import org.dicio.component.standard.StandardResult;
import org.dicio.component.util.WordExtractor;
import org.dicio.dicio_android.components.AssistanceComponent;
import org.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import org.dicio.dicio_android.output.speech.SpeechOutputDevice;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.dicio.component.InputRecognizer.Specificity.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ComponentRankerTest {

    final static String input = "hi";
    final static List<String> inputWords = WordExtractor.extractWords(input);

    private static final class TestAC implements AssistanceComponent {
        final InputRecognizer.Specificity specificity;
        final float score;
        String input;
        
        public TestAC(final InputRecognizer.Specificity specificity, final float score) {
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
        public void setInput(final String input, final List<String> inputWords) {
            this.input = input;
        }
        public String getInput() {
            return input;
        }

        // useless for this test
        @Override public void processInput(final Locale locale) {}
        @Override public void generateOutput(
                final Context context, final SpeechOutputDevice speechOutputDevice,
                final GraphicalOutputDevice graphicalOutputDevice) {}
        @Override public void cleanup() {}
    }


    private static ComponentRanker getRanker(final AssistanceComponent fallback, final AssistanceComponent... others) {
        return new ComponentRanker(Arrays.asList(others), fallback);
    }

    private static void assertRanked(final ComponentRanker cr,
                                     final TestAC fallback,
                                     final TestAC best) {
        final AssistanceComponent result = cr.getBest("", new ArrayList<>());
        String message;
        if (result == fallback) {
            message = "Fallback component returned by getBest";
        } else {
            message = "Component with specificity " + result.specificity()
                    + " and score " + result.score() + " returned by getBest";
        }

        assertSame(message, best, result);
    }


    @Test
    public void testOrderPreservedAndCorrectInput() {
        TestAC  ac1 =   new TestAC(high,   0.80f),
                ac2 =   new TestAC(high,   0.93f),
                ac3 =   new TestAC(high,   1.00f),
                acMed = new TestAC(medium, 1.00f),
                acLow = new TestAC(low,    1.00f);

        ComponentRanker cr = getRanker(new TestAC(low, 0.0f), ac1, acMed, ac2, acLow, ac3);
        cr.getBest(input, inputWords);

        assertEquals(input, ac1.getInput());
        assertEquals(input, ac2.getInput());
        assertNull(ac3.getInput());
        assertNull(acMed.getInput());
        assertNull(acLow.getInput());
    }

    @Test
    public void testHighPrHighScore() {
        final TestAC fallback = new TestAC(low, 0.0f);
        final TestAC best = new TestAC(high, 0.92f);
        final ComponentRanker cr = getRanker(fallback,
                new TestAC(medium, 0.95f), new TestAC(high, 0.71f),
                best, new TestAC(high, 1.00f), new TestAC(low, 1.0f));
        assertRanked(cr, fallback, best);
    }


    @Test
    public void testHighPrLowScore() {
        final TestAC fallback = new TestAC(low, 0.0f);
        final TestAC best = new TestAC(low, 1.0f);
        final ComponentRanker cr = getRanker(fallback,
                new TestAC(medium, 0.81f), new TestAC(high, 0.71f),
                new TestAC(low, 0.85f), best, new TestAC(high, 0.32f));
        assertRanked(cr, fallback, best);
    }

    @Test
    public void testFallback() {
        TestAC fallback = new TestAC(low, 0.0f);

        ComponentRanker cr = getRanker(fallback, new TestAC(low, 0.8f));
        TestAC result = (TestAC) cr.getBest(input, inputWords);

        assertSame("Fallback component not returned by getBest", result, fallback);
        assertEquals(input, result.getInput());
    }
}