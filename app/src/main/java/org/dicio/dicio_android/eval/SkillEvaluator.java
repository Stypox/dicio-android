package org.dicio.dicio_android.eval;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.Sections;
import org.dicio.dicio_android.input.InputDevice;
import org.dicio.dicio_android.input.SpeechInputDevice.UnableToAccessMicrophoneException;
import org.dicio.dicio_android.input.ToolbarInputDevice;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.skills.SkillHandler;
import org.dicio.dicio_android.util.ExceptionUtils;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.util.WordExtractor;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SkillEvaluator {

    private final SkillRanker skillRanker;
    private final InputDevice primaryInputDevice;
    @Nullable private final ToolbarInputDevice secondaryInputDevice;
    private final SpeechOutputDevice speechOutputDevice;
    private final GraphicalOutputDevice graphicalOutputDevice;
    private final Context context;

    boolean currentlyProcessingInput = false;
    final Queue<String> queuedInputs = new LinkedList<>();
    @Nullable View partialInputView = null;
    @Nullable private Disposable evaluationDisposable = null;


    public SkillEvaluator(final SkillRanker skillRanker,
                          final InputDevice primaryInputDevice,
                          @Nullable final ToolbarInputDevice secondaryInputDevice,
                          final SpeechOutputDevice speechOutputDevice,
                          final GraphicalOutputDevice graphicalOutputDevice,
                          final Context context) {

        this.skillRanker = skillRanker;
        this.primaryInputDevice = primaryInputDevice;
        this.secondaryInputDevice = secondaryInputDevice;
        this.speechOutputDevice = speechOutputDevice;
        this.graphicalOutputDevice = graphicalOutputDevice;
        this.context = context;

        setupInputDeviceListeners();
        showInitialScreen();
    }


    ////////////////////
    // Public methods //
    ////////////////////

    public void cleanup() {
        primaryInputDevice.setOnInputReceivedListener(null);
        if (secondaryInputDevice != null) {
            secondaryInputDevice.setOnInputReceivedListener(null);
        }

        if (evaluationDisposable != null) {
            evaluationDisposable.dispose();
        }
        queuedInputs.clear();
        partialInputView = null;
    }

    public InputDevice getPrimaryInputDevice() {
        return primaryInputDevice;
    }

    @Nullable
    public ToolbarInputDevice getSecondaryInputDevice() {
        return secondaryInputDevice;
    }


    ///////////
    // Setup //
    ///////////

    private void setupInputDeviceListeners() {
        final InputDevice.OnInputReceivedListener onInputReceivedListener =
                new InputDevice.OnInputReceivedListener() {
                    @Override
                    public void onPartialInputReceived(final String input) {
                        displayPartialUserInput(input);
                    }

                    @Override
                    public void onInputReceived(final String input) {
                        processInput(input);
                    }

                    @Override
                    public void onError(final Throwable e) {
                        SkillEvaluator.this.onError(e);
                    }
                };

        primaryInputDevice.setOnInputReceivedListener(onInputReceivedListener);
        if (secondaryInputDevice != null) {
            secondaryInputDevice.setOnInputReceivedListener(onInputReceivedListener);
        }
    }

    private void showInitialScreen() {
        final View initialScreen = GraphicalOutputUtils.inflate(context, R.layout.initial_screen);

        final LinearLayout skillItemsLayout = initialScreen.findViewById(R.id.skillItemsLayout);
        for (final SkillInfo skillInfo : SkillHandler.getRandomSkillInfoList(6)) {
            final View skillInfoItem
                    = GraphicalOutputUtils.inflate(context, R.layout.initial_screen_skill_item);

            ((AppCompatImageView) skillInfoItem.findViewById(R.id.skillIconImageView))
                    .setImageResource(SkillHandler.getSkillIconResource(skillInfo));
            ((AppCompatTextView) skillInfoItem.findViewById(R.id.skillName))
                    .setText(skillInfo.getNameResource());
            ((AppCompatTextView) skillInfoItem.findViewById(R.id.skillSentenceExample))
                    .setText(skillInfo.getSentenceExampleResource());

            skillItemsLayout.addView(skillInfoItem);
        }

        graphicalOutputDevice.displayTemporary(initialScreen);
    }


    ///////////////////
    // Partial input //
    ///////////////////

    private void displayPartialUserInput(final String input) {
        if (partialInputView == null) {
            partialInputView =
                    GraphicalOutputUtils.inflate(context, R.layout.user_input_partial);
        }
        final TextView textView = partialInputView.findViewById(R.id.userInput);
        textView.setText(input);
        graphicalOutputDevice.displayTemporary(partialInputView);
    }



    //////////////////////
    // Input processing //
    //////////////////////

    private void processInput(final String input) {
        queuedInputs.add(input);
        tryToProcessQueuedInput();
    }

    private void finishedProcessingInput() {
        queuedInputs.poll(); // current input has finished processing
        currentlyProcessingInput = false;
        tryToProcessQueuedInput(); // try to process next input, if present
    }

    private void tryToProcessQueuedInput() {
        if (currentlyProcessingInput) {
            return;
        }

        final String input = queuedInputs.peek();
        if (input != null) {
            currentlyProcessingInput = true;
            displayUserInput(input.replace('\n', ' '));
            evaluateMatchingSkill(input);
        }
    }

    private void displayUserInput(final String input) {
        final View userInputView =
                GraphicalOutputUtils.inflate(context, R.layout.user_input);
        final AppCompatEditText inputEditText = userInputView.findViewById(R.id.userInput);

        inputEditText.setText(input);

        inputEditText.addTextChangedListener(new TextWatcher() {
            // `count` characters beginning at `start` replaced old text that had length `before`
            int startIndex = 0, countBefore = 0, countAfter = 0;
            boolean ignore = false;

            @Override public void beforeTextChanged(final CharSequence s, final int start,
                                                    final int count, final int after) {}

            @Override public void onTextChanged(final CharSequence s, final int start,
                                                final int before, final int count) {
                if (ignore) {
                    return;
                }

                startIndex = start;
                countBefore = before;
                countAfter = count;
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (ignore) {
                     return;
                }

                if (countBefore == 0 && countAfter == 1) {
                    // a single character has just been inserted, most probably from the keyboard,
                    // so check if it is the Enter key (i.e. the newline '\n')

                    if (s.length() > startIndex && s.charAt(startIndex) == '\n') {
                        s.delete(startIndex, startIndex + 1);
                        if (!s.toString().trim().equals(input.trim())) {
                            processInput(s.toString());
                            inputEditText.setText(input); // restore original input
                        }
                    }
                } else {
                    // text was copy-pasted, so replace all newlines with spaces

                    if (s.length() < startIndex + countAfter) {
                        return; // should be impossible, but just to be sure
                    }

                    // do all at once for performance and otherwise `s.replace` indices get messed
                    final char[] chars = new char[countAfter];
                    s.getChars(startIndex, startIndex + countAfter, chars, 0);
                    for (int i = 0; i < countAfter; ++i) {
                        if (chars[i] == '\n') {
                            chars[i] = ' ';
                        }
                    }

                    ignore = true; // ignore the calls made to this listener by `s.replace`
                    s.replace(startIndex, startIndex + countAfter, new String(chars));
                    ignore = false;
                }
            }
        });

        userInputView.setOnClickListener(v -> {
            // focus test pointer after last character
            inputEditText.requestFocus();
            @Nullable final Editable text = inputEditText.getText();
            inputEditText.setSelection(text == null ? 0 : text.length());

            // open keyboard
            final InputMethodManager inputMethodManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(inputEditText, InputMethodManager.SHOW_FORCED);
        });

        graphicalOutputDevice.display(userInputView);
    }



    //////////////////////
    // Skill evaluation //
    //////////////////////

    private void evaluateMatchingSkill(final String input) {
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
            primaryInputDevice.tryToGetInput();
        }

        skill.cleanup(); // cleanup the input that was set
        finishedProcessingInput();
    }


    ///////////
    // Error //
    ///////////

    private void onError(final Throwable t) {
        t.printStackTrace();

        if (ExceptionUtils.hasAssignableCause(t, UnableToAccessMicrophoneException.class)) {
            speechOutputDevice.speak(context.getString(R.string.microphone_error));
            graphicalOutputDevice.display(GraphicalOutputUtils.buildDescription(context,
                    context.getString(R.string.microphone_error)));
        } else if (ExceptionUtils.isNetworkError(t)) {
            speechOutputDevice.speak(context.getString(R.string.eval_network_error_description));
            graphicalOutputDevice.display(GraphicalOutputUtils.buildNetworkErrorMessage(context));
        } else {
            skillRanker.removeAllBatches();
            speechOutputDevice.speak(context.getString(R.string.eval_fatal_error));
            graphicalOutputDevice.display(GraphicalOutputUtils.buildErrorMessage(context, t));
        }
        graphicalOutputDevice.addDivider();

        finishedProcessingInput();
    }
}
