package com.dicio.dicio_android.io.speech;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ToastSpeechDevice implements SpeechOutputDevice {
    Context context;

    public ToastSpeechDevice(Context context) {
        this.context = context;
    }

    @Override
    public void speak(@NonNull String speechOutput) {
        Toast.makeText(context, speechOutput, Toast.LENGTH_LONG).show();
    }
}
