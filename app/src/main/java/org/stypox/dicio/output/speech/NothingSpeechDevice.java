package org.stypox.dicio.output.speech;

import androidx.annotation.NonNull;

public class NothingSpeechDevice extends InstantSpeechDevice {
    @Override
    public void speak(@NonNull final String speechOutput) {
        // do nothing
    }

    @Override
    public void stopSpeaking() {
    }

    @Override
    public void cleanup() {
    }
}
