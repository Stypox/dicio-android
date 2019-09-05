package com.dicio.dicio_android.eval;

import android.content.Context;

import com.dicio.component.AssistanceComponent;
import com.dicio.dicio_android.renderer.OutputDisplayer;
import com.dicio.dicio_android.renderer.OutputRenderer;

import java.util.List;

public class ComponentEvaluator {

    private final ComponentRanker componentRanker;
    private final OutputDisplayer outputDisplayer;
    private final Context context;

    public ComponentEvaluator(ComponentRanker componentRanker, OutputDisplayer outputDisplayer, Context context) {
        this.componentRanker = componentRanker;
        this.outputDisplayer = outputDisplayer;
        this.context = context;
    }

    public void evaluateMatchingComponent(String input) throws NoSuchFieldException, IllegalAccessException {
        List<String> words = WordExtractor.extractWords(input);
        AssistanceComponent component = componentRanker.getBest(words);

        outputDisplayer.addSpeechOutput(component.getSpeechOutput());
        outputDisplayer.addGraphicalOutput(OutputRenderer.renderComponentOutput(component, context));
    }
}
