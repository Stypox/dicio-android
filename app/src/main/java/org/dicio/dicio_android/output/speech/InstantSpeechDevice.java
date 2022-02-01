package org.dicio.dicio_android.output.speech;

import org.dicio.skill.output.SpeechOutputDevice;

/**
 * A speech device that speaks instantaneously (e.g. because it just displays text). Overrides
 * {@link #isSpeaking()} so that it always returns false, and {@link
 * #runWhenFinishedSpeaking(Runnable)} so that the runnable is run instantly.
 */
public abstract class InstantSpeechDevice implements SpeechOutputDevice {
    @Override
    public final boolean isSpeaking() {
        return false;
    }

    @Override
    public final void runWhenFinishedSpeaking(final Runnable runnable) {
        runnable.run();
    }
}
