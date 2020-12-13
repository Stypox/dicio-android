package org.dicio.dicio_android.output.speech;

import androidx.annotation.NonNull;

public interface SpeechOutputDevice {
    /**
     * Speaks the provided text
     * @param speechOutput what to tell to the user
     */
    void speak(@NonNull String speechOutput);
}
