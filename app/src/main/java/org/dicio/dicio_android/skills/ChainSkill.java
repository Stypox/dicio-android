package org.dicio.dicio_android.skills;

import android.content.Context;

import org.dicio.component.InputRecognizer;
import org.dicio.component.IntermediateProcessor;
import org.dicio.dicio_android.output.OutputGenerator;
import org.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import org.dicio.dicio_android.output.speech.SpeechOutputDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChainSkill implements Skill {

    public static class Builder {
        ChainSkill chainSkill;

        public Builder() {
            chainSkill = new ChainSkill();
        }

        public Builder recognize(final InputRecognizer<?> inputRecognizer) {
            if (chainSkill.inputRecognizer != null) {
                throw new IllegalArgumentException("recognize() can only be called once");
            }

            chainSkill.inputRecognizer = inputRecognizer;
            return this;
        }

        public Builder process(final IntermediateProcessor<?, ?> intermediateProcessor) {
            if (chainSkill.inputRecognizer == null) {
                throw new IllegalArgumentException("process() can't be called before recognize()");
            }

            chainSkill.intermediateProcessors.add(intermediateProcessor);
            return this;
        }

        public ChainSkill output(final OutputGenerator<?> outputGenerator) {
            if (chainSkill.inputRecognizer == null) {
                throw new IllegalArgumentException("output() can't be called before recognize()");
            }

            chainSkill.outputGenerator = outputGenerator;
            return chainSkill;
        }
    }


    private InputRecognizer inputRecognizer;
    private final List<IntermediateProcessor> intermediateProcessors;
    private OutputGenerator outputGenerator;
    private Object lastResult;

    private ChainSkill() {
        intermediateProcessors = new ArrayList<>();
    }


    @Override
    public InputRecognizer.Specificity specificity() {
        return inputRecognizer.specificity();
    }

    @Override
    public void setInput(final String input,
                         final List<String> inputWords,
                         final List<String> normalizedWordKeys) {
        inputRecognizer.setInput(input, inputWords, normalizedWordKeys);
    }

    @Override
    public float score() {
        return inputRecognizer.score();
    }

    @Override
    public void cleanup() {
        inputRecognizer.cleanup();
        lastResult = null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void processInput(final Locale locale) throws Exception {
        lastResult = inputRecognizer.getResult();

        for (int i = 0; i < intermediateProcessors.size(); ++i) {
            lastResult = intermediateProcessors.get(i).process(lastResult, locale);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void generateOutput(final Context context,
                               final SpeechOutputDevice speechOutputDevice,
                               final GraphicalOutputDevice graphicalOutputDevice) {
        outputGenerator.generate(lastResult, context, speechOutputDevice, graphicalOutputDevice);
    }

    /**
     * @see OutputGenerator#nextSkills()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Skill> nextSkills() {
        return outputGenerator.nextSkills();
    }
}
