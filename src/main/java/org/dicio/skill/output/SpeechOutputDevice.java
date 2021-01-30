package org.dicio.skill.output;

import androidx.annotation.NonNull;

import org.dicio.skill.util.CleanableUp;

public interface SpeechOutputDevice extends CleanableUp {
    /**
     * Speaks the provided text
     * @param speechOutput what to tell to the user
     */
    void speak(@NonNull String speechOutput);
}
