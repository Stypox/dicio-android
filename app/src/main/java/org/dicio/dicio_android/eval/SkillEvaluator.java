package org.dicio.dicio_android.eval;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;

import org.dicio.component.util.WordExtractor;
import org.dicio.dicio_android.R;
import org.dicio.dicio_android.skills.Skill;
import org.dicio.dicio_android.input.InputDevice;
import org.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.output.speech.SpeechOutputDevice;
import org.dicio.dicio_android.Sections;
import org.dicio.dicio_android.util.ExceptionUtils;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SkillEvaluator {
    private final SkillRanker skillRanker;
    private final InputDevice inputDevice;
    private final SpeechOutputDevice speechOutputDevice;
    private final GraphicalOutputDevice graphicalOutputDevice;
    private final Context context;

    @Nullable private Disposable evaluationDisposable = null;

    public SkillEvaluator(final SkillRanker skillRanker,
                              final InputDevice inputDevice,
                              final SpeechOutputDevice speechOutputDevice,
                              final GraphicalOutputDevice graphicalOutputDevice,
                              final Context context) {

        this.skillRanker = skillRanker;
        this.inputDevice = inputDevice;
        this.speechOutputDevice = speechOutputDevice;
        this.graphicalOutputDevice = graphicalOutputDevice;
        this.context = context;

        inputDevice.setOnInputReceivedListener(new InputDevice.OnInputReceivedListener() {
            @Override
            public void onInputReceived(final String input) {
                processInput(input);
            }

            @Override
            public void onError(final Throwable e) {
                SkillEvaluator.this.onError(e);
            }
        });
    }

    public void processInput(final String input) {
        displayUserInput(input);
        evaluateMatchingSkill(input);
    }

    public void displayUserInput(final String input) {
        final View userInputView =
                GraphicalOutputUtils.inflate(context, R.layout.skill_user_input);
        final EditText inputEditText = userInputView.findViewById(R.id.userInput);

        inputEditText.setText(input);
        inputEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                if (!v.getText().toString().trim().equals(input.trim())) {
                    processInput(v.getText().toString());
                    inputEditText.setText(input); // restore original input
                }
                return true;
            }
            return false;
        });

        userInputView.setOnClickListener(v -> {
            inputEditText.requestFocus();
            inputEditText.setSelection(inputEditText.getText().length());
            final InputMethodManager inputMethodManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(inputEditText, InputMethodManager.SHOW_FORCED);
        });

        graphicalOutputDevice.display(userInputView);
    }

    public void evaluateMatchingSkill(final String input) {
        if (evaluationDisposable != null && !evaluationDisposable.isDisposed()) {
            evaluationDisposable.dispose();
        }

        evaluationDisposable = Single
                .fromCallable(() -> {
                    final List<String> inputWords = WordExtractor.extractWords(input);
                    final List<String> normalizedWordKeys =
                            WordExtractor.normalizeWords(inputWords);
                    final Skill skill = skillRanker.getBest(
                            input, inputWords, normalizedWordKeys);

                    skill.processInput(Sections.getCurrentLocale());
                    return skill;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::generateOutput, this::onError);
    }

    private void generateOutput(final Skill skill) {
        skill.generateOutput(context, speechOutputDevice, graphicalOutputDevice);
        graphicalOutputDevice.addDivider();
        skill.cleanup(); // cleanup the input that was set

        final List<Skill> nextSkills =
                skill.nextSkills();
        if (nextSkills.isEmpty()) {
            // current conversation has ended, reset to the default batch of skills
            skillRanker.removeAllBatches();
        } else {
            skillRanker.addBatchToTop(nextSkills);
            inputDevice.tryToGetInput();
        }
    }

    private void onError(final Throwable t) {
        t.printStackTrace();

        if (ExceptionUtils.isNetworkError(t)) {
            speechOutputDevice.speak(context.getString(R.string.eval_network_error_description));
            graphicalOutputDevice.display(GraphicalOutputUtils.buildNetworkErrorMessage(context));
        } else {
            skillRanker.removeAllBatches();
            speechOutputDevice.speak(context.getString(R.string.eval_fatal_error));
            graphicalOutputDevice.display(GraphicalOutputUtils.buildErrorMessage(context, t));
        }
        graphicalOutputDevice.addDivider();
    }
}
