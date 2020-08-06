package org.dicio.dicio_android.eval;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dicio.dicio_android.components.AssistanceComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ComponentRanker {
    // various thresholds for different specificity categories (high, medium and low)
    private static final float
            // first round
            highThreshold1   = 0.85f,
            // second round
            mediumThreshold2 = 0.90f,
            highThreshold2   = 0.80f,
            // third round
            lowThreshold3    = 0.90f,
            mediumThreshold3 = 0.80f,
            highThreshold3   = 0.70f;


    private static class ComponentScoreResult {
        final AssistanceComponent component;
        final float score;

        ComponentScoreResult(final AssistanceComponent component, final float score) {
            this.component = component;
            this.score = score;
        }
    }

    private static class ComponentBatch {
        // all of the components by specificity category (high, medium and low)
        private final List<AssistanceComponent> highComponents;
        private final List<AssistanceComponent> mediumComponents;
        private final List<AssistanceComponent> lowComponents;

        ComponentBatch(final List<AssistanceComponent> components) {
            highComponents = new ArrayList<>();
            mediumComponents = new ArrayList<>();
            lowComponents = new ArrayList<>();

            for (final AssistanceComponent component : components) {
                switch (component.specificity()) {
                    case high:
                        highComponents.add(component);
                        break;
                    case medium:
                        mediumComponents.add(component);
                        break;
                    case low:
                        lowComponents.add(component);
                        break;
                }
            }
        }

        private static ComponentScoreResult getFirstAboveThresholdOrBest(
                final List<AssistanceComponent> components,
                final String input,
                final List<String> inputWords,
                final List<String> normalizedWordKeys,
                final float threshold) {
            // this ensures that if `components` is empty and null component is returned,
            // nothing bad happens since its score cannot be higher than any other float value.
            float bestScoreSoFar = Float.MIN_VALUE;
            AssistanceComponent bestComponentSoFar = null;

            for (AssistanceComponent component : components) {
                component.setInput(input, inputWords, normalizedWordKeys);
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
        AssistanceComponent getBest(final String input,
                                    final List<String> inputWords,
                                    final List<String> normalizedWordKeys) {
            // first round: considering only high-priority components
            final ComponentScoreResult bestHigh = getFirstAboveThresholdOrBest(
                    highComponents, input, inputWords, normalizedWordKeys, highThreshold1);
            if (bestHigh.score > highThreshold1) {
                return bestHigh.component;
            }

            // second round: considering both medium- and high-priority components
            final ComponentScoreResult bestMedium = getFirstAboveThresholdOrBest(
                    mediumComponents, input, inputWords, normalizedWordKeys, mediumThreshold2);
            if (bestMedium.score > mediumThreshold2) {
                return bestMedium.component;
            } else if (bestHigh.score > highThreshold2) {
                return bestHigh.component;
            }

            // third round: all components are considered
            final ComponentScoreResult bestLow = getFirstAboveThresholdOrBest(
                    lowComponents, input, inputWords, normalizedWordKeys, lowThreshold3);
            if (bestLow.score > lowThreshold3) {
                return bestLow.component;
            } else if (bestMedium.score > mediumThreshold3) {
                return bestMedium.component;
            } else if (bestHigh.score > highThreshold3) {
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

    public AssistanceComponent getBest(final String input,
                                       final List<String> inputWords,
                                       final List<String> normalizedWordKeys) {
        for(int i = batches.size() - 1; i >= 0; --i) {
            final AssistanceComponent componentFromBatch =
                    batches.get(i).getBest(input, inputWords, normalizedWordKeys);
            if (componentFromBatch != null) {
                return componentFromBatch;
            }
        }

        final AssistanceComponent componentFromDefault =
                defaultBatch.getBest(input, inputWords, normalizedWordKeys);
        if (componentFromDefault == null) {
            // nothing was matched
            fallbackComponent.setInput(input, inputWords, normalizedWordKeys);
            return fallbackComponent;
        } else {
            return componentFromDefault;
        }
    }
}
