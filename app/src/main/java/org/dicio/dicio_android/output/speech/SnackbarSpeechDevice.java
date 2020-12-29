package org.dicio.dicio_android.output.speech;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import org.dicio.skill.output.SpeechOutputDevice;

public class SnackbarSpeechDevice implements SpeechOutputDevice {
    private final View view;

    public SnackbarSpeechDevice(final View view) {
        this.view = view;
    }

    @Override
    public void speak(@NonNull final String speechOutput) {
        Snackbar.make(view, speechOutput, Snackbar.LENGTH_LONG).show();
    }
}
