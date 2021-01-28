package org.dicio.dicio_android.eval;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.Sections;
import org.dicio.dicio_android.input.InputDevice;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.util.ExceptionUtils;
import org.dicio.skill.Skill;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.util.WordExtractor;

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
        final AppCompatEditText inputEditText = userInputView.findViewById(R.id.userInput);

        inputEditText.setText(input);

        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(final CharSequence s, final int start,
                                                    final int count, final int after) {}
            @Override public void onTextChanged(final CharSequence s, final int start,
                                                final int before, final int count) {}

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() != 0 && s.charAt(s.length() - 1) == '\n') {
                    s.delete(s.length() - 1, s.length());
                    if (!s.toString().trim().equals(input.trim())) {
                        processInput(s.toString());
                        inputEditText.setText(input); // restore original input
                    }
                }
            }
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

                    skill.processInput(context,
                            PreferenceManager.getDefaultSharedPreferences(context),
                            Sections.getCurrentLocale());
                    return skill;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::generateOutput, this::onError);
    }

    private void generateOutput(final Skill skill) {
        skill.generateOutput(context, PreferenceManager.getDefaultSharedPreferences(context),
                Sections.getCurrentLocale(), speechOutputDevice, graphicalOutputDevice);
        graphicalOutputDevice.addDivider();

        final List<Skill> nextSkills = skill.nextSkills();
        if (nextSkills == null || nextSkills.isEmpty()) {
            // current conversation has ended, reset to the default batch of skills
            skillRanker.removeAllBatches();
        } else {
            skillRanker.addBatchToTop(nextSkills);
            inputDevice.tryToGetInput();
        }

        skill.cleanup(); // cleanup the input that was set
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
