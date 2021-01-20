package org.dicio.dicio_android.output.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.dicio.dicio_android.R;
import org.dicio.skill.output.SpeechOutputDevice;

import java.util.Locale;

public class AndroidTtsSpeechDevice implements SpeechOutputDevice {

    private final Context context;
    private TextToSpeech textToSpeech = null;
    private boolean initializedCorrectly = false;

    public AndroidTtsSpeechDevice(final Context context, final Locale locale) {
        this.context = context;
        textToSpeech = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                if (textToSpeech.setLanguage(locale) >= 0) { // errors are -1 or -2
                    initializedCorrectly = true;
                } else {
                    Toast.makeText(context, R.string.android_tts_unsupported_language,
                            Toast.LENGTH_SHORT).show();
                    textToSpeech.shutdown();
                    textToSpeech = null;
                }
            }
        });
    }

    @Override
    public void speak(@NonNull final String speechOutput) {
        if (initializedCorrectly) {
            textToSpeech.speak(speechOutput, TextToSpeech.QUEUE_ADD, null, null);
        } else {
            Toast.makeText(context, speechOutput, Toast.LENGTH_LONG).show();
        }
    }
}
