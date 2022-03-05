package org.dicio.dicio_android.output.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.dicio.dicio_android.R;
import org.dicio.skill.output.SpeechOutputDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AndroidTtsSpeechDevice implements SpeechOutputDevice {

    private Context context;
    private TextToSpeech textToSpeech = null;
    private boolean initializedCorrectly = false;
    private final List<Runnable> runnablesWhenFinished = new ArrayList<>();
    private int lastUtteranceId = 0;

    public AndroidTtsSpeechDevice(final Context context, final Locale locale) {
        this.context = context;
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                if (textToSpeech.setLanguage(locale) >= 0) { // errors are -1 or -2
                    initializedCorrectly = true;
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(final String utteranceId) {
                        }

                        @Override
                        public void onDone(final String utteranceId) {
                            if (("dicio_" + lastUtteranceId).equals(utteranceId)) {
                                // run only when the last enqueued utterance is finished
                                for (final Runnable runnable : runnablesWhenFinished) {
                                    runnable.run();
                                }
                                runnablesWhenFinished.clear();
                            }
                        }

                        @Override
                        public void onError(final String utteranceId) {
                        }
                    });
                    return;
                } else {
                    Toast.makeText(context, R.string.android_tts_unsupported_language,
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, R.string.android_tts_error, Toast.LENGTH_SHORT).show();
            }

            if (textToSpeech != null) {
                textToSpeech.shutdown();
                textToSpeech = null;
            }
        });
    }

    @Override
    public void speak(@NonNull final String speechOutput) {
        if (initializedCorrectly) {
            lastUtteranceId += 1;
            textToSpeech.speak(speechOutput, TextToSpeech.QUEUE_ADD, null,
                    "dicio_" + lastUtteranceId);
        } else {
            Toast.makeText(context, speechOutput, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void stopSpeaking() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    @Override
    public boolean isSpeaking() {
        return textToSpeech != null && textToSpeech.isSpeaking();
    }

    @Override
    public void runWhenFinishedSpeaking(final Runnable runnable) {
        if (isSpeaking()) {
            runnablesWhenFinished.add(runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public void cleanup() {
        context = null;
        if (textToSpeech != null) {
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
}
