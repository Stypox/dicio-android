package org.dicio.dicio_android.output.speech;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.dicio.skill.output.SpeechOutputDevice;

public class ToastSpeechDevice implements SpeechOutputDevice {
    private Context context;

    public ToastSpeechDevice(final Context context) {
        this.context = context;
    }

    @Override
    public void speak(@NonNull final String speechOutput) {
        Toast.makeText(context, speechOutput, Toast.LENGTH_LONG).show();
    }

    @Override
    public void cleanup() {
        context = null;
    }
}
