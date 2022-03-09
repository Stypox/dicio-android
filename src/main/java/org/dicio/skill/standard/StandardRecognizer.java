package org.dicio.skill.standard;

import org.dicio.skill.chain.InputRecognizer;

import java.util.Collections;
import java.util.List;

public class StandardRecognizer extends InputRecognizer<StandardResult> {
    private final StandardRecognizerData data;
    private String input;
    private List<String> inputWords;
    private List<String> normalizedInputWords;

    private PartialScoreResult bestResultSoFar;
    private String bestSentenceIdSoFar;


    /////////////////
    // Constructor //
    /////////////////

    public StandardRecognizer(final StandardRecognizerData data) {
        this.data = data;
        this.inputWords = Collections.emptyList();
        this.normalizedInputWords = Collections.emptyList();
    }

    public StandardRecognizer(final InputRecognizer.Specificity specificity,
                              final Sentence[] sentences) {
        this(new StandardRecognizerData(specificity, sentences));
    }


    ///////////////////////////////
    // InputRecognizer overrides //
    ///////////////////////////////

    @Override
    public Specificity specificity() {
        return data.getSpecificity();
    }

    @Override
    public void setInput(final String input,
                         final List<String> inputWords,
                         final List<String> normalizedInputWords) {

        this.input = input;
        this.inputWords = inputWords;
        this.normalizedInputWords = normalizedInputWords;
    }

    @Override
    public float score() {
        bestResultSoFar = data.getSentences()[0].score(inputWords, normalizedInputWords);
        float bestValueSoFar = bestResultSoFar.value(inputWords.size());
        bestSentenceIdSoFar = data.getSentences()[0].getSentenceId();

        for (int i = 1; i < data.getSentences().length; ++i) {
            final PartialScoreResult result =
                    data.getSentences()[i].score(inputWords, normalizedInputWords);
            final float value = result.value(inputWords.size());

            final boolean valuesAlmostEqual = Math.abs(value - bestValueSoFar) < 0.01f;
            final boolean lessWordsInCapturingGroups = result.getWordsInCapturingGroups()
                    < bestResultSoFar.getWordsInCapturingGroups();

            if ((valuesAlmostEqual && lessWordsInCapturingGroups) || value > bestValueSoFar) {
                // update the best result so far also if new result evaluates approximately equal
                // but has less words in capturing groups
                bestResultSoFar = result;
                bestValueSoFar = value;
                bestSentenceIdSoFar = data.getSentences()[i].getSentenceId();
            }
        }

        return bestResultSoFar.value(inputWords.size());
    }

    @Override
    public StandardResult getResult() {
        return bestResultSoFar.toStandardResult(bestSentenceIdSoFar, input);
    }

    @Override
    public void cleanup() {
        input = null;
        inputWords = Collections.emptyList();
        normalizedInputWords = Collections.emptyList();
        bestResultSoFar = null;
        bestSentenceIdSoFar = null;
    }
}
