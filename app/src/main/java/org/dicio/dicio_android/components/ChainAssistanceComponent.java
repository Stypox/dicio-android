package org.dicio.dicio_android.components;

import android.content.Context;

import org.dicio.component.InputRecognizer;
import org.dicio.component.IntermediateProcessor;
import org.dicio.dicio_android.output.OutputGenerator;
import org.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import org.dicio.dicio_android.output.speech.SpeechOutputDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChainAssistanceComponent implements AssistanceComponent {

    public static class Builder {
        ChainAssistanceComponent chainAssistanceComponent;

        public Builder() {
            chainAssistanceComponent = new ChainAssistanceComponent();
        }

        public Builder recognize(final InputRecognizer<?> inputRecognizer) {
            if (chainAssistanceComponent.inputRecognizer != null) {
                throw new IllegalArgumentException("recognize() can only be called once");
            }

            chainAssistanceComponent.inputRecognizer = inputRecognizer;
            return this;
        }

        public Builder process(final IntermediateProcessor<?, ?> intermediateProcessor) {
            if (chainAssistanceComponent.inputRecognizer == null) {
                throw new IllegalArgumentException("process() can't be called before recognize()");
            }

            chainAssistanceComponent.intermediateProcessors.add(intermediateProcessor);
            return this;
        }

        public ChainAssistanceComponent output(final OutputGenerator<?> outputGenerator) {
            if (chainAssistanceComponent.inputRecognizer == null) {
                throw new IllegalArgumentException("output() can't be called before recognize()");
            }

            chainAssistanceComponent.outputGenerator = outputGenerator;
            return chainAssistanceComponent;
        }
    }


    private InputRecognizer inputRecognizer;
    private final List<IntermediateProcessor> intermediateProcessors;
    private OutputGenerator outputGenerator;
    private Object lastResult;

    private ChainAssistanceComponent() {
        intermediateProcessors = new ArrayList<>();
    }


    @Override
    public InputRecognizer.Specificity specificity() {
        return inputRecognizer.specificity();
    }

    @Override
    public void setInput(final String input, final List<String> inputWords) {
        inputRecognizer.setInput(input, inputWords);
    }

    @Override
    public float score() {
        return inputRecognizer.score();
    }

    @Override
    public void cleanup() {
        inputRecognizer.cleanup();
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
     * @see OutputGenerator#nextAssistanceComponents()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<AssistanceComponent> nextAssistanceComponents() {
        return outputGenerator.nextAssistanceComponents();
    }
}
