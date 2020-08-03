package org.dicio.dicio_android.output.speech;

import androidx.annotation.NonNull;

public interface SpeechOutputDevice {
    void speak(@NonNull String speechOutput);
}
