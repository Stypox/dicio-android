package org.dicio.skill.chain;

import org.dicio.skill.standard.StandardResult;

import java.util.List;

/**
 * A recognizer that always matches any input and results in a {@link StandardResult} on which
 * calling {@link StandardResult#getCapturingGroup(String)} always returns the input. The
 * specificity is low and the score is always 1.0.
 */
public class CaptureEverythingRecognizer extends InputRecognizer<StandardResult> {

    private String input;

    @Override
    public Specificity specificity() {
        return Specificity.low;
    }

    @Override
    public void setInput(final String input,
                         final List<String> inputWords,
                         final List<String> normalizedInputWords) {
        this.input = input;
    }

    @Override
    public float score() {
        return 1.0f;
    }

    @Override
    public StandardResult getResult() {
        return new StandardResult("", input, null) {
            @Override
            public String getCapturingGroup(final String name) {
                return input;
            }
        };
    }

    @Override
    public void cleanup() {
    }
}
