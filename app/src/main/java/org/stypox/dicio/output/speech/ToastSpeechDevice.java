package org.stypox.dicio.output.speech;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ToastSpeechDevice extends InstantSpeechDevice {
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
