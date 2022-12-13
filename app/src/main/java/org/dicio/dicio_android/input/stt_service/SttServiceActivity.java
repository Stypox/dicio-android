package org.dicio.dicio_android.input.stt_service;

import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.databinding.DialogSttServiceBinding;
import org.dicio.dicio_android.error.ErrorInfo;
import org.dicio.dicio_android.error.ErrorUtils;
import org.dicio.dicio_android.error.UserAction;
import org.dicio.dicio_android.input.InputDevice;
import org.dicio.dicio_android.input.SpeechInputDevice;
import org.dicio.dicio_android.input.VoskInputDevice;
import org.dicio.dicio_android.util.BaseActivity;
import org.dicio.dicio_android.util.ShareUtils;
import org.dicio.dicio_android.util.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.preference.PreferenceManager;

import static android.speech.RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT;
import static android.speech.RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE;

public class SttServiceActivity extends BaseActivity {
    private static final String TAG = SttServiceActivity.class.getSimpleName();

    AppCompatDialog dialog;
    SpeechInputDevice speechInputDevice;
    DialogSttServiceBinding binding;

    private SharedPreferences preferences;

    boolean startedForSpeechResult = false;
    private Bundle speechExtras;

    @Override
    protected int getThemeFromPreferences() {
        return ThemeUtils.chooseThemeBasedOnPreferences(this,
                R.style.SttServiceLightAppTheme, R.style.SttServiceLightAppTheme);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        final Intent intent = getIntent();
        if (intent != null && RecognizerIntent.ACTION_RECOGNIZE_SPEECH.equals(intent.getAction())) {
            startedForSpeechResult = true;
            speechExtras = intent.getExtras();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Context wrappedContext = new ContextThemeWrapper(this,
                ThemeUtils.chooseThemeBasedOnPreferences(this,
                        R.style.LightAppTheme, R.style.DarkAppTheme));
        final LayoutInflater layoutInflater = LayoutInflater.from(wrappedContext);
        binding = DialogSttServiceBinding.inflate(layoutInflater);
        final String prompt = speechExtras.getString(
                RecognizerIntent.EXTRA_PROMPT, getString(R.string.stt_say_something));
        binding.userInput.setHint(prompt);

        dialog = new BottomSheetDialog(wrappedContext);
        dialog.setCancelable(true);
        dialog.setOnDismissListener(d -> finish());
        dialog.setContentView(binding.getRoot());
        dialog.create();
        dialog.show();

        setupSpeechInputDevice();
        setupButtons();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        binding = null;
        if (speechInputDevice != null) {
            speechInputDevice.cleanup();
            speechInputDevice = null;
        }
    }


    private void setupSpeechInputDevice() {
        //TODO Extras which may be also useful for speech recognition, sorted by priority:
        // EXTRA_LANGUAGE_MODEL (abel with vosk?), EXTRA_BIASING_STRINGS, EXTRA_LANGUAGE,
        // EXTRA_AUDIO_SOURCE, DETAILS_META_DATA(?)
        speechInputDevice = new VoskInputDevice(this);
        speechInputDevice.setVoiceViews(binding.voiceFab, binding.voiceLoading);
        speechInputDevice.tryToGetInput(false);
        speechInputDevice.setInputDeviceListener(new InputDevice.InputDeviceListener() {
            @Override
            public void onTryingToGetInput() {
                final String prompt = speechExtras.getString(
                        RecognizerIntent.EXTRA_PROMPT, getString(R.string.stt_say_something));
                binding.userInput.setHint(prompt);
                binding.userInput.setEnabled(false);
            }

            @Override
            public void onPartialInputReceived(final String input) {
                showUserInput(input);
            }

            @Override
            public void onInputReceived(final List<String> input) {
                showUserInput(input.get(0));
                binding.userInput.setEnabled(true);
                final boolean autoFinish = preferences
                        .getBoolean(getString(R.string.pref_key_stt_auto_finish), false);
                if (startedForSpeechResult && autoFinish) {
                    sendSpeechResult();
                    dialog.dismiss();
                }
            }

            @Override
            public void onNoInputReceived() {
                binding.userInput.setHint(R.string.stt_did_not_understand);
                binding.userInput.setEnabled(true);
            }

            @Override
            public void onError(final Throwable e) {
                ErrorUtils.createNotification(SttServiceActivity.this,
                        new ErrorInfo(e, UserAction.STT_SERVICE_SPEECH_TO_TEXT));
                binding.userInput.setEnabled(true);
            }
        });
    }

    private void setupButtons() {
        binding.copyButton.setOnClickListener(v -> {
            ShareUtils.copyToClipboard(this, getUserInput());
            dialog.dismiss();
        });

        final Context context = binding.getRoot().getContext();
        if (startedForSpeechResult) {
            binding.doneOrShareButton.setImageResource(
                    ThemeUtils.resolveResourceIdFromAttr(context, R.attr.iconDone));
            binding.doneOrShareButton.setOnClickListener(v -> {
                sendSpeechResult();
                dialog.dismiss();
            });
        } else {
            binding.doneOrShareButton.setImageResource(
                    ThemeUtils.resolveResourceIdFromAttr(context, R.attr.iconShare));
            binding.doneOrShareButton.setOnClickListener(v -> {
                ShareUtils.shareText(this, "", getUserInput());
                dialog.dismiss();
            });
        }
    }


    private void showUserInput(final String userInput) {
        binding.userInput.setText(userInput.trim());
    }

    private String getUserInput() {
        final CharSequence charSequence = binding.userInput.getText();
        if (charSequence == null) {
            return "";
        } else {
            return charSequence.toString();
        }
    }

    public void sendSpeechResult() {
        // get results from recognizer and prepare for reporting
        final ArrayList<String> foundTexts = new ArrayList<>();
        foundTexts.add(getUserInput());

        // Because there is currently just one result, it gets 1.0 TODO check how to get more
        // results + confidence from vosk. When extending number of results,
        // implement EXTRA_MAX_RESULTS

        final ArrayList<Float> confidenceScore = new ArrayList<>();
        confidenceScore.add(1.0f);

        // Prepare Result Intent with Extras
        final Intent intent = new Intent();
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, foundTexts);
        intent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, confidenceScore);

        // This is for some apps, who use the Android SearchManager (e.g. ebay)
        // (in my eyes probably wrong implemented by them, however without it's not working...)
        intent.putExtra(SearchManager.QUERY, getUserInput());

        setResult(RESULT_OK, intent);
        if (speechExtras != null && speechExtras.containsKey(EXTRA_RESULTS_PENDINGINTENT)) {
            if (speechExtras.containsKey(EXTRA_RESULTS_PENDINGINTENT_BUNDLE)) {
                intent.getExtras()
                        .putAll(speechExtras.getBundle(EXTRA_RESULTS_PENDINGINTENT_BUNDLE));
            }
            final PendingIntent resultIntent =
                    speechExtras.getParcelable(EXTRA_RESULTS_PENDINGINTENT);

            try {
                resultIntent.send(this, RESULT_OK, intent);
            } catch (final PendingIntent.CanceledException e) {
                Log.w(TAG, "Speech result pending intent canceled", e);
            }
        }
    }
}
