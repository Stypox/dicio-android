package org.dicio.dicio_android.output.speech;

import androidx.annotation.NonNull;

public class NothingSpeechDevice implements SpeechOutputDevice {
    @Override
    public void speak(@NonNull final String speechOutput) {
        // do nothing
    }
}
