package com.dicio.dicio_android.components;

import android.content.Context;

import com.dicio.component.InputRecognizer;
import com.dicio.component.IntermediateProcessor;
import com.dicio.dicio_android.output.OutputGenerator;
import com.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import com.dicio.dicio_android.output.speech.SpeechOutputDevice;

import java.util.ArrayList;
import java.util.List;

public class ChainAssistanceComponent implements AssistanceComponent {

    public static class Builder {
        ChainAssistanceComponent chainAssistanceComponent;

        public Builder() {
            chainAssistanceComponent = new ChainAssistanceComponent();
        }

        public Builder recognize(InputRecognizer<?> inputRecognizer) {
            if (chainAssistanceComponent.inputRecognizer != null) {
                throw new IllegalArgumentException("recognize() can only be called once");
            }

            chainAssistanceComponent.inputRecognizer = inputRecognizer;
            return this;
        }

        public Builder process(IntermediateProcessor<?, ?> intermediateProcessor) {
            if (chainAssistanceComponent.inputRecognizer == null) {
                throw new IllegalArgumentException("process() can't be called before recognize()");
            }

            chainAssistanceComponent.intermediateProcessors.add(intermediateProcessor);
            return this;
        }

        public ChainAssistanceComponent output(OutputGenerator<?> outputGenerator) {
            if (chainAssistanceComponent.inputRecognizer == null) {
                throw new IllegalArgumentException("output() can't be called before recognize()");
            }

            chainAssistanceComponent.outputGenerator = outputGenerator;
            return chainAssistanceComponent;
        }
    }


    private InputRecognizer inputRecognizer;
    private List<IntermediateProcessor> intermediateProcessors;
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
    public void setInput(List<String> words) {
        inputRecognizer.setInput(words);
    }

    @Override
    public float score() {
        return inputRecognizer.score();
    }


    @Override
    public void processInput() throws Exception {
        lastResult = inputRecognizer.getResult();

        for (int i = 0; i < intermediateProcessors.size(); ++i) {
            lastResult = intermediateProcessors.get(i).process(lastResult);
        }
    }

    @Override
    public void generateOutput(Context context,
                               SpeechOutputDevice speechOutputDevice,
                               GraphicalOutputDevice graphicalOutputDevice) {
        outputGenerator.generate(lastResult, context, speechOutputDevice, graphicalOutputDevice);
    }

    /**
     * @see OutputGenerator#nextAssistanceComponents()
     */
    @Override
    public List<AssistanceComponent> nextAssistanceComponents() {
        return outputGenerator.nextAssistanceComponents();
    }
}
