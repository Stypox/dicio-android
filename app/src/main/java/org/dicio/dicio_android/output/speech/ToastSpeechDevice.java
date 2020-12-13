package org.dicio.dicio_android.output.speech;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ToastSpeechDevice implements SpeechOutputDevice {
    private final Context context;

    public ToastSpeechDevice(final Context context) {
        this.context = context;
    }

    @Override
    public void speak(@NonNull final String speechOutput) {
        Toast.makeText(context, speechOutput, Toast.LENGTH_LONG).show();
    }
}
