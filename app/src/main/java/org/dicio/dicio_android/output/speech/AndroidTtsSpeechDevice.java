package org.dicio.dicio_android.output.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;

import org.dicio.skill.output.SpeechOutputDevice;

import java.util.Locale;

public class AndroidTtsSpeechDevice implements SpeechOutputDevice {

    private TextToSpeech textToSpeech = null;
    private boolean initializedCorrectly = false;

    public AndroidTtsSpeechDevice(final Context context, final Locale locale) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                if (textToSpeech.setLanguage(locale) >= 0) { // errors are -1 or -2
                    initializedCorrectly = true;
                } else {
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
        }
    }
}
