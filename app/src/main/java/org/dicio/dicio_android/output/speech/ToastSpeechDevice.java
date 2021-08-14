package org.dicio.dicio_android.output.speech;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.dicio.skill.output.SpeechOutputDevice;

public class ToastSpeechDevice implements SpeechOutputDevice {
    private Context context;
    private Toast currentToast = null;

    public ToastSpeechDevice(final Context context) {
        this.context = context;
    }

    @Override
    public void speak(@NonNull final String speechOutput) {
        currentToast = Toast.makeText(context, speechOutput, Toast.LENGTH_LONG);
        currentToast.show();
    }

    @Override
    public void stopSpeaking() {
        if (currentToast != null) {
            currentToast.cancel();
        }
    }

    @Override
    public void cleanup() {
        context = null;
    }
}
