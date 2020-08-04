package org.dicio.dicio_android.eval;

import android.content.Context;

import org.dicio.component.util.WordExtractor;
import org.dicio.dicio_android.R;
import org.dicio.dicio_android.components.AssistanceComponent;
import org.dicio.dicio_android.input.InputDevice;
import org.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.output.speech.SpeechOutputDevice;
import org.dicio.dicio_android.util.ExceptionUtils;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ComponentEvaluator {
    private final ComponentRanker componentRanker;
    private final InputDevice inputDevice;
    private final SpeechOutputDevice speechOutputDevice;
    private final GraphicalOutputDevice graphicalOutputDevice;
    private final Context context;

    private Disposable evaluationDisposable;

    public ComponentEvaluator(ComponentRanker componentRanker,
                              InputDevice inputDevice,
                              SpeechOutputDevice speaker,
                              GraphicalOutputDevice displayer,
                              Context context) {

        this.componentRanker = componentRanker;
        this.inputDevice = inputDevice;
        this.speechOutputDevice = speaker;
        this.graphicalOutputDevice = displayer;
        this.context = context;

        inputDevice.setOnInputReceivedListener(new InputDevice.OnInputReceivedListener() {
            @Override
            public void onInputReceived(String input) {
                evaluateMatchingComponent(input);
            }

            @Override
            public void onError(Throwable e) {
                ComponentEvaluator.this.onError(e);
            }
        });
    }

    public void evaluateMatchingComponent(String input) {
        if (evaluationDisposable != null && !evaluationDisposable.isDisposed()) {
            evaluationDisposable.dispose();
        }

        evaluationDisposable = Single
                .fromCallable(() -> {
                    AssistanceComponent component = componentRanker.getBest(
                            input, WordExtractor.extractWords(input));
                    // TODO let user choose locale to use for component processing and output
                    component.processInput(context.getResources().getConfiguration().locale);
                    return component;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::generateOutput, this::onError);
    }

    private void generateOutput(AssistanceComponent component) {
        component.generateOutput(context, speechOutputDevice, graphicalOutputDevice);

        List<AssistanceComponent> nextAssistanceComponents = component.nextAssistanceComponents();
        if (nextAssistanceComponents.isEmpty()) {
            // current conversation has ended, reset to the default batch of components
            componentRanker.removeAllBatches();
        } else {
            componentRanker.addBatchToTop(nextAssistanceComponents);
            inputDevice.tryToGetInput();
        }
    }

    private void onError(Throwable t) {
        t.printStackTrace();

        if (ExceptionUtils.isNetworkError(t)) {
            speechOutputDevice.speak(context.getString(R.string.eval_network_error_description));
            graphicalOutputDevice.display(GraphicalOutputUtils.buildNetworkErrorMessage(context));
        } else {
            componentRanker.removeAllBatches();
            speechOutputDevice.speak(context.getString(R.string.eval_fatal_error));
            graphicalOutputDevice.display(GraphicalOutputUtils.buildErrorMessage(context, t));
        }
    }
}
