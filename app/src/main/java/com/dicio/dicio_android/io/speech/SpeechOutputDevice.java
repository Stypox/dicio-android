package com.dicio.dicio_android.io.speech;

import androidx.annotation.NonNull;

public interface SpeechOutputDevice {
    void speak(@NonNull String speechOutput);
}
