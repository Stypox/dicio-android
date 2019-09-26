package com.dicio.dicio_android.eval;

import android.content.Context;

import com.dicio.component.AssistanceComponent;
import com.dicio.component.output.OutputGenerator;
import com.dicio.dicio_android.R;
import com.dicio.dicio_android.io.graphical.GraphicalOutputDevice;
import com.dicio.dicio_android.io.graphical.render.OutputRenderer;
import com.dicio.dicio_android.io.input.InputDevice;
import com.dicio.dicio_android.io.speech.SpeechOutputDevice;

import java.util.List;
import java.util.concurrent.Callable;

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

    public ComponentEvaluator(ComponentRanker componentRanker, InputDevice inputDevice, SpeechOutputDevice speaker, GraphicalOutputDevice displayer, Context context) {
        this.componentRanker = componentRanker;
        this.inputDevice = inputDevice;
        this.speechOutputDevice = speaker;
        this.graphicalOutputDevice = displayer;
        this.context = context;

        inputDevice.setOnInputReceivedListener(this::evaluateMatchingComponent);
    }

    public void evaluateMatchingComponent(String input) {
        if (evaluationDisposable != null && !evaluationDisposable.isDisposed()) {
            evaluationDisposable.dispose();
        }

        evaluationDisposable = Single
                .fromCallable(() -> {
                    List<String> words = WordExtractor.extractWords(input);
                    AssistanceComponent component = componentRanker.getBest(words);
                    component.calculateOutput();
                    return component;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::generateOutput, this::onError);
    }

    private void generateOutput(OutputGenerator component) throws NoSuchFieldException, IllegalAccessException {
        speechOutputDevice.speak(component.getSpeechOutput());
        graphicalOutputDevice.display(OutputRenderer.renderComponentOutput(component, context));

        if (component.nextOutputGenerator().isPresent()) {
            generateOutput(component.nextOutputGenerator().get());
        } else if (component.nextAssistanceComponents().isPresent()) {
            componentRanker.addBatchToTop(component.nextAssistanceComponents().get());
            inputDevice.startListening();
        } else {
            // current conversation has ended, reset to the default batch of components
            componentRanker.removeAllBatches();
        }
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();

        componentRanker.removeAllBatches();
        speechOutputDevice.speak(context.getString(R.string.error_while_evaluating));
        graphicalOutputDevice.display(OutputRenderer.renderError(throwable, context));
    }
}
