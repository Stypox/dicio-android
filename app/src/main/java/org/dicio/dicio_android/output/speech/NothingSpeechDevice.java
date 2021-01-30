package org.dicio.dicio_android.output.speech;

import androidx.annotation.NonNull;

import org.dicio.skill.output.SpeechOutputDevice;

public class NothingSpeechDevice implements SpeechOutputDevice {
    @Override
    public void speak(@NonNull final String speechOutput) {
        // do nothing
    }

    @Override
    public void cleanup() {
    }
}
