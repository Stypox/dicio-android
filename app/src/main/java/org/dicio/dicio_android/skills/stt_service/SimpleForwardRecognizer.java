package org.dicio.dicio_android.skills.stt_service;

import org.dicio.skill.chain.InputRecognizer;

import java.util.Collections;
import java.util.List;

/**
 * This Recognizer just forwards the speech input completely
 */
public class SimpleForwardRecognizer  extends InputRecognizer<SimpleForwardRecognizer.Result> {
    public class Result {
        public String input;
        public List<String> inputWords;
        public List<String> normalizedInputWords;
    }


    private String input;
    private List<String> inputWords;
    private List<String> normalizedInputWords;

    public SimpleForwardRecognizer() {
        this.inputWords = Collections.emptyList();
        this.normalizedInputWords = Collections.emptyList();
    }

    @Override
    public Specificity specificity() {
        return Specificity.high;
    }

    @Override
    public void setInput(final String input, final List<String> inputWords,
                         final List<String> normalizedInputWords) {
        this.input = input;
        this.inputWords = inputWords;
        this.normalizedInputWords = normalizedInputWords;
    }

    @Override
    public float score() {
        return 1.0f;
    }

    @Override
    public SimpleForwardRecognizer.Result getResult() {
        final Result r = new Result();
        r.input = input;
        r.inputWords = inputWords;
        r.normalizedInputWords = normalizedInputWords;
        return r;
    }

    @Override
    public void cleanup() {

    }
}
