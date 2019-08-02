package com.dicio.dicio_android.sorter;

import com.dicio.component.AssistanceComponent;
import com.dicio.component.input.InputRecognitionUnit.Specificity;
import com.dicio.component.output.OutputGenerationUnit;
import com.dicio.component.output.views.ViewList;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dicio.component.input.InputRecognitionUnit.Specificity.high;
import static com.dicio.component.input.InputRecognitionUnit.Specificity.low;
import static com.dicio.component.input.InputRecognitionUnit.Specificity.medium;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ComponentSorterTest {
    private AssistanceComponent comp(final Specificity specificity, final float score) {
        return new AssistanceComponent() {
            List<String> input = null;

            // useless in this tests
            public void calculateOutput() {}
            public ViewList getGraphicalOutput() { return null; }
            public String getSpeechOutput() { return null; }
            public Optional<OutputGenerationUnit> nextOutputGenerator() { return Optional.empty(); }
            public Optional<List<AssistanceComponent>> nextAssistanceComponents() { return Optional.empty(); }

            @Override public void setInput(List<String> words) { input = words; }
            @Override public List<String> getInput()           { return input; }
            @Override public Specificity specificity()         { return specificity; }
            @Override public float score()                     { return score; }
        };
    }
    private ComponentSorter getSorter(AssistanceComponent fallback, final AssistanceComponent... others) {
        return new ComponentSorter(fallback) {{
            addAll(others);
        }};
    }


    @Test
    public void testOrderPreservedAndCorrectInput() {
        AssistanceComponent
                ac1 = comp(high, 0.80f),
                ac2 = comp(high, 0.93f),
                ac3 = comp(high, 1.00f),
                acMed = comp(medium, 1.00f),
                acLow = comp(low, 1.00f);
        List<String> words = new ArrayList<String>() {{ add("hi"); }};

        ComponentSorter cs = getSorter(comp(low, 0.0f), ac1, acMed);
        cs.addAll(ac2, acLow);
        cs.add(ac3);
        cs.getBest(words);

        assertArrayEquals(words.toArray(), ac1.getInput().toArray());
        assertArrayEquals(words.toArray(), ac2.getInput().toArray());
        assertNull(ac3.getInput());
        assertNull(acMed.getInput());
        assertNull(acLow.getInput());
    }

    @Test
    public void testHighPrHighScore() {
        AssistanceComponent fallback = comp(low, 0.0f);
        AssistanceComponent best = comp(high, 0.92f);
        ComponentSorter cs = getSorter(fallback,
                comp(medium, 0.95f), comp(high, 0.71f), best, comp(high, 1.00f), comp(low, 1.0f));

        AssistanceComponent result = cs.getBest(new ArrayList<String>());
        String message;
        if (result == fallback) {
            message = "Fallback component returned by getBest";
        } else {
            message = "Component with specificity " + result.specificity() + " and score " + result.score() + " returned by getBest";
        }

        assertSame(message, best, result);
    }


    @Test
    public void testHighPrLowScore() {
        AssistanceComponent fallback = comp(low, 0.0f);
        AssistanceComponent best = comp(low, 1.0f);
        ComponentSorter cs = getSorter(fallback,
                comp(medium, 0.81f), comp(high, 0.71f), comp(low, 0.85f), best, comp(high, 0.32f));

        AssistanceComponent result = cs.getBest(new ArrayList<String>());
        String message;
        if (result == fallback) {
            message = "Fallback component returned by getBest";
        } else {
            message = "Component with specificity " + result.specificity() + " and score " + result.score() + " returned by getBest";
        }

        assertSame(message, best, result);
    }

    @Test
    public void testFallback() {
        AssistanceComponent fallback = comp(low, 0.0f);
        List<String> words = new ArrayList<String>() {{ add("hi"); }};

        ComponentSorter cs = getSorter(fallback, comp(low, 0.8f));
        AssistanceComponent result = cs.getBest(words);

        assertSame("Fallback component not returned by getBest", result, fallback);
        assertArrayEquals(words.toArray(), result.getInput().toArray());
    }
}