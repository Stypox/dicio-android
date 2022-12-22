package org.stypox.dicio.eval;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import org.stypox.dicio.MainActivity;
import org.stypox.dicio.R;
import org.stypox.dicio.input.InputDevice;
import org.stypox.dicio.input.SpeechInputDevice.UnableToAccessMicrophoneException;
import org.stypox.dicio.input.ToolbarInputDevice;
import org.stypox.dicio.output.graphical.GraphicalOutputUtils;
import org.stypox.dicio.skills.SkillHandler;
import org.stypox.dicio.error.ExceptionUtils;
import org.stypox.dicio.util.PermissionUtils;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.util.CleanableUp;
import org.dicio.skill.util.WordExtractor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SkillEvaluator implements CleanableUp {

    private final SkillRanker skillRanker;
    private final InputDevice primaryInputDevice;
    @Nullable private final ToolbarInputDevice secondaryInputDevice;
    private final SpeechOutputDevice speechOutputDevice;
    private final GraphicalOutputDevice graphicalOutputDevice;
    private Activity activity;

    private boolean currentlyProcessingInput = false;
    private final Queue<List<String>> queuedInputs = new LinkedList<>();
    @Nullable private View partialInputView = null;
    private boolean hasAddedPartialInputView = false;
    @Nullable private Disposable evaluationDisposable = null;
    @Nullable private Skill skillNeedingPermissions = null;


    public SkillEvaluator(final SkillRanker skillRanker,
                          final InputDevice primaryInputDevice,
                          @Nullable final ToolbarInputDevice secondaryInputDevice,
                          final SpeechOutputDevice speechOutputDevice,
                          final GraphicalOutputDevice graphicalOutputDevice,
                          final Activity activity) {

        this.skillRanker = skillRanker;
        this.primaryInputDevice = primaryInputDevice;
        this.secondaryInputDevice = secondaryInputDevice;
        this.speechOutputDevice = speechOutputDevice;
        this.graphicalOutputDevice = graphicalOutputDevice;
        this.activity = activity;

        setupInputDeviceListeners();
        setupOutputDevices();
    }


    ////////////////////
    // Public methods //
    ////////////////////

    public void showInitialPanel() {
        final View initialPanel = GraphicalOutputUtils.inflate(activity, R.layout.initial_panel);

        final LinearLayout skillItemsLayout = initialPanel.findViewById(R.id.skillItemsLayout);
        for (final SkillInfo skillInfo : SkillHandler.getEnabledSkillInfoListShuffled()) {
            final View skillInfoItem
                    = GraphicalOutputUtils.inflate(activity, R.layout.initial_panel_skill_item);

            ((AppCompatImageView) skillInfoItem.findViewById(R.id.skillIconImageView))
                    .setImageResource(SkillHandler.getSkillIconResource(skillInfo));
            ((AppCompatTextView) skillInfoItem.findViewById(R.id.skillName))
                    .setText(skillInfo.getNameResource());
            ((AppCompatTextView) skillInfoItem.findViewById(R.id.skillSentenceExample))
                    .setText(skillInfo.getSentenceExampleResource());

            skillItemsLayout.addView(skillInfoItem);
        }

        graphicalOutputDevice.displayTemporary(initialPanel);
    }

    @Override
    public void cleanup() {
        cancelGettingInput();
        skillRanker.cleanup();

        primaryInputDevice.cleanup();
        if (secondaryInputDevice != null) {
            secondaryInputDevice.cleanup();
        }
        speechOutputDevice.cleanup();
        graphicalOutputDevice.cleanup();
        activity = null;

        if (evaluationDisposable != null) {
            evaluationDisposable.dispose();
        }
        skillNeedingPermissions = null;
        queuedInputs.clear();
        partialInputView = null;
    }

    public void cancelGettingInput() {
        primaryInputDevice.cancelGettingInput();
        if (secondaryInputDevice != null) {
            secondaryInputDevice.cancelGettingInput();
        }
    }

    public InputDevice getPrimaryInputDevice() {
        return primaryInputDevice;
    }

    @Nullable
    public ToolbarInputDevice getSecondaryInputDevice() {
        return secondaryInputDevice;
    }

    /**
     * to be called from the main thread
     */
    public void onSkillRequestPermissionsResult(@NonNull final int[] grantResults) {
        if (skillNeedingPermissions == null) {
            return; // should be unreachable
        }
        final Skill skill = skillNeedingPermissions;
        // make sure this skill is not reprocessed (also should never happen, but who knows)
        skillNeedingPermissions = null;

        if (PermissionUtils.areAllPermissionsGranted(grantResults)) {
            evaluationDisposable = Single.fromCallable(() -> {
                skill.processInput();
                return skill;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::generateOutput, this::onError);

        } else {
            // permissions were not granted, show a message
            @Nullable final SkillInfo skillInfo = skill.getSkillInfo();
            if (skillInfo != null) {
                // skill info will always be non-null, but stay on the safe side and add a check
                final String message = activity.getString(R.string.eval_missing_permissions,
                        activity.getString(skillInfo.getNameResource()),
                        PermissionUtils.getCommaJoinedPermissions(activity, skillInfo));
                speechOutputDevice.speak(message);
                graphicalOutputDevice.display(
                        GraphicalOutputUtils.buildDescription(activity, message));
            }

            graphicalOutputDevice.addDivider();
            finishedProcessingInput();
        }
    }


    ///////////
    // Setup //
    ///////////

    private void setupInputDeviceListeners() {
        primaryInputDevice.setInputDeviceListener(new InputDevice.InputDeviceListener() {
            @Override
            public void onTryingToGetInput() {
                speechOutputDevice.stopSpeaking();
                if (secondaryInputDevice != null) {
                    secondaryInputDevice.cancelGettingInput();
                }
            }

            @Override
            public void onPartialInputReceived(final String input) {
                displayPartialUserInput(input);
            }

            @Override
            public void onInputReceived(final List<String> input) {
                processInput(input);
            }

            @Override
            public void onNoInputReceived() {
                handleNoInput();
            }

            @Override
            public void onError(final Throwable e) {
                SkillEvaluator.this.onError(e);
            }
        });

        if (secondaryInputDevice != null) {
            secondaryInputDevice.setInputDeviceListener(new InputDevice.InputDeviceListener() {
                @Override
                public void onTryingToGetInput() {
                    speechOutputDevice.stopSpeaking();
                    primaryInputDevice.cancelGettingInput();
                }

                @Override
                public void onPartialInputReceived(final String input) {
                    displayPartialUserInput(input);
                }

                @Override
                public void onInputReceived(final List<String> input) {
                    processInput(input);
                }


                @Override
                public void onNoInputReceived() {
                    handleNoInput();
                }

                @Override
                public void onError(final Throwable e) {
                    SkillEvaluator.this.onError(e);
                }
            });
        }
    }

    private void setupOutputDevices() {
        // this adds a divider only if there are already some output views (can happen when
        // reloading the skill evaluator)
        graphicalOutputDevice.addDivider();
    }


    /////////////////////////////////////////
    // Partial input and no input handling //
    /////////////////////////////////////////

    private void displayPartialUserInput(final String input) {
        hasAddedPartialInputView = true;
        if (partialInputView == null) {
            partialInputView = GraphicalOutputUtils.inflate(activity, R.layout.user_input_partial);
        }
        final TextView textView = partialInputView.findViewById(R.id.userInput);
        textView.setText(input);
        graphicalOutputDevice.displayTemporary(partialInputView);
    }

    private void handleNoInput() {
        if (hasAddedPartialInputView) {
            // remove temporary partial input view: no input was provided
            graphicalOutputDevice.removeTemporary();
            hasAddedPartialInputView = false;
        }
    }



    //////////////////////
    // Input processing //
    //////////////////////

    private void processInput(final List<String> input) {
        hasAddedPartialInputView = false;
        queuedInputs.add(input);
        tryToProcessQueuedInput();
    }

    private void finishedProcessingInput() {
        queuedInputs.poll(); // current input has finished processing
        currentlyProcessingInput = false;
        tryToProcessQueuedInput(); // try to process next input, if present
    }

    private void tryToProcessQueuedInput() {
        if (currentlyProcessingInput || queuedInputs.isEmpty()) {
            return;
        }

        currentlyProcessingInput = true;
        evaluateMatchingSkill(queuedInputs.peek());
    }

    private void displayUserInput(final String input) {
        final View userInputView =
                GraphicalOutputUtils.inflate(activity, R.layout.user_input);
        final AppCompatEditText inputEditText = userInputView.findViewById(R.id.userInput);

        inputEditText.setText(input);

        inputEditText.addTextChangedListener(new TextWatcher() {
            // `count` characters beginning at `start` replaced old text that had length `before`
            int startIndex = 0;
            int countBefore = 0;
            int countAfter = 0;
            boolean ignore = false;

            @Override public void beforeTextChanged(final CharSequence s, final int start,
                                                    final int count, final int after) {
            }

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
                            processInput(Collections.singletonList(s.toString()));
                            inputEditText.setText(input); // restore original input
                            inputEditText.clearFocus(); // prevent focus problems
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
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(inputEditText, InputMethodManager.SHOW_FORCED);
        });

        graphicalOutputDevice.display(userInputView);
    }



    //////////////////////
    // Skill evaluation //
    //////////////////////

    private static class InputSkillPair {
        final String input;
        final Skill skill;
        String[] permissionsToRequest = null;

        InputSkillPair(final String input, final Skill skill) {
            this.input = input;
            this.skill = skill;
        }
    }

    private void evaluateMatchingSkill(final List<String> inputs) {
        if (evaluationDisposable != null && !evaluationDisposable.isDisposed()) {
            evaluationDisposable.dispose();
        }

        evaluationDisposable = Single.fromCallable(() -> {
            final InputSkillPair chosen = inputs.stream()
                    .map(input -> {
                        final List<String> inputWords = WordExtractor.extractWords(input);
                        final List<String> normalizedWords
                                = WordExtractor.normalizeWords(inputWords);
                        return new InputSkillPair(input,
                                skillRanker.getBest(input, inputWords, normalizedWords));
                    })
                    .filter(inputSkillPair -> inputSkillPair.skill != null)
                    .findFirst()
                    .orElseGet((Supplier<InputSkillPair>) (() -> {
                        final List<String> inputWords = WordExtractor.extractWords(inputs.get(0));
                        final List<String> normalizedWords
                                = WordExtractor.normalizeWords(inputWords);
                        return new InputSkillPair(inputs.get(0), skillRanker.getFallbackSkill(
                                inputs.get(0), inputWords, normalizedWords));
                    }));

            final String[] permissions = PermissionUtils.permissionsArrayFromSkill(chosen.skill);
            if (PermissionUtils.checkPermissions(activity, permissions)) {
                // skill's output will be generated below, so process input now
                chosen.skill.processInput();
            } else {
                // before executing this skill needs some permissions, don't process input now
                chosen.permissionsToRequest = permissions;
            }

            return chosen;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onChosenSkill, this::onError);
    }

    private void onChosenSkill(final InputSkillPair chosen) {
        displayUserInput(chosen.input);

        if (chosen.permissionsToRequest == null) {
            generateOutput(chosen.skill);
        } else {
            // request permissions; when done process input in onSkillRequestPermissionsResult
            // note: need to do this here on main thread
            ActivityCompat.requestPermissions(activity, chosen.permissionsToRequest,
                    MainActivity.SKILL_PERMISSIONS_REQUEST_CODE);
            skillNeedingPermissions = chosen.skill;
        }
    }

    private void generateOutput(final Skill skill) {
        skill.generateOutput();

        final List<Skill> nextSkills = skill.nextSkills();
        if (nextSkills == null || nextSkills.isEmpty()) {
            // current conversation has ended, reset to the default batch of skills
            skillRanker.removeAllBatches();
            graphicalOutputDevice.addDivider();
        } else {
            skillRanker.addBatchToTop(nextSkills);
            speechOutputDevice.runWhenFinishedSpeaking(
                    () -> activity.runOnUiThread(
                            () -> primaryInputDevice.tryToGetInput(false)));
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
            final String message = activity.getString(R.string.microphone_error);
            speechOutputDevice.speak(message);
            graphicalOutputDevice.display(
                    GraphicalOutputUtils.buildDescription(activity, message));
        } else if (ExceptionUtils.isNetworkError(t)) {
            speechOutputDevice.speak(activity.getString(R.string.eval_network_error_description));
            graphicalOutputDevice.display(GraphicalOutputUtils.buildNetworkErrorMessage(activity));
        } else {
            skillRanker.removeAllBatches();
            speechOutputDevice.speak(activity.getString(R.string.eval_fatal_error));
            graphicalOutputDevice.display(GraphicalOutputUtils.buildErrorMessage(activity, t));
        }
        graphicalOutputDevice.addDivider();

        finishedProcessingInput();
    }
}
