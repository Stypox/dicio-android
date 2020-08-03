package org.dicio.dicio_android.eval;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dicio.dicio_android.components.AssistanceComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ComponentRanker {
    private static final float
            // first round
            highPrThreshold1   = 0.85f,
            // second round
            mediumPrThreshold2 = 0.90f,
            highPrThreshold2   = 0.80f,
            // third round
            lowPrThreshold3    = 0.90f,
            mediumPrThreshold3 = 0.80f,
            highPrThreshold3   = 0.70f;


    private static class ComponentScoreResult {
        AssistanceComponent component;
        float score;
        ComponentScoreResult(AssistanceComponent component, float score) {
            this.component = component;
            this.score = score;
        }
    }

    private static class ComponentBatch {
        private List<AssistanceComponent> highPrComponents;
        private List<AssistanceComponent> mediumPrComponents;
        private List<AssistanceComponent> lowPrComponents;

        ComponentBatch(List<AssistanceComponent> components) {
            highPrComponents = new ArrayList<>();
            mediumPrComponents = new ArrayList<>();
            lowPrComponents = new ArrayList<>();

            for (AssistanceComponent component : components) {
                switch (component.specificity()) {
                    case high:
                        highPrComponents.add(component);
                        break;
                    case medium:
                        mediumPrComponents.add(component);
                        break;
                    case low:
                        lowPrComponents.add(component);
                        break;
                }
            }
        }

        private static ComponentScoreResult getFirstAboveThresholdOrBest(
                final List<AssistanceComponent> components,
                final String input,
                final List<String> inputWords,
                final float threshold) {
            /* this ensures that if `components` is empty and null component is returned,
            nothing bad happens since its score cannot be higher than any other float value. */
            float bestScoreSoFar = Float.MIN_VALUE;
            AssistanceComponent bestComponentSoFar = null;

            for (AssistanceComponent component : components) {
                component.setInput(input, inputWords);
                float score = component.score();

                if (score > bestScoreSoFar) {
                    bestScoreSoFar = score;
                    bestComponentSoFar = component;
                    if (score > threshold) {
                        break;
                    }
                }
            }

            return new ComponentScoreResult(bestComponentSoFar, bestScoreSoFar);
        }

        @Nullable
        AssistanceComponent getBest(final String input, final List<String> inputWords) {
            // first round: considering only high-priority components
            final ComponentScoreResult bestHigh = getFirstAboveThresholdOrBest(
                    highPrComponents, input, inputWords, highPrThreshold1);
            if (bestHigh.score > highPrThreshold1) {
                return bestHigh.component;
            }

            // second round: considering both medium- and high-priority components
            final ComponentScoreResult bestMedium = getFirstAboveThresholdOrBest(
                    mediumPrComponents, input, inputWords, mediumPrThreshold2);
            if (bestMedium.score > mediumPrThreshold2) {
                return bestMedium.component;
            } else if (bestHigh.score > highPrThreshold2) {
                return bestHigh.component;
            }

            // third round: all components are considered
            final ComponentScoreResult bestLow = getFirstAboveThresholdOrBest(
                    lowPrComponents, input, inputWords, lowPrThreshold3);
            if (bestLow.score > lowPrThreshold3) {
                return bestLow.component;
            } else if (bestMedium.score > mediumPrThreshold3) {
                return bestMedium.component;
            } else if (bestHigh.score > highPrThreshold3) {
                return bestHigh.component;
            }

            // nothing was matched
            return null;
        }
    }

    @NonNull
    private ComponentBatch defaultBatch;
    @NonNull
    private AssistanceComponent fallbackComponent;
    @NonNull
    private Stack<ComponentBatch> batches;

    public ComponentRanker(List<AssistanceComponent> defaultComponentBatch,
                           @NonNull AssistanceComponent fallbackComponent) {
        this.defaultBatch = new ComponentBatch(defaultComponentBatch);
        this.fallbackComponent = fallbackComponent;
        this.batches = new Stack<>();
    }

    public void addBatchToTop(List<AssistanceComponent> componentBatch) {
        batches.push(new ComponentBatch(componentBatch));
    }

    public void removeTopBatch() {
        batches.pop();
    }

    public void removeAllBatches() {
        batches.removeAllElements();
    }

    public AssistanceComponent getBest(final String input, final List<String> inputWords) {
        for(int i = batches.size() - 1; i >= 0; --i) {
            AssistanceComponent componentFromBatch = batches.get(i).getBest(input, inputWords);
            if (componentFromBatch != null) {
                return componentFromBatch;
            }
        }

        final AssistanceComponent componentFromDefault = defaultBatch.getBest(input, inputWords);
        if (componentFromDefault == null) {
            // nothing was matched
            fallbackComponent.setInput(input, inputWords);
            return fallbackComponent;
        } else {
            return componentFromDefault;
        }
    }
}
