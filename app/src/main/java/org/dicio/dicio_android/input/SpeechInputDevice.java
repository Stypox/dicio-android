package org.dicio.dicio_android.input;

import android.graphics.drawable.Drawable;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public abstract class SpeechInputDevice extends InputDevice {
    private ExtendedFloatingActionButton voiceFab;
    private Drawable microphoneOnIcon;
    private Drawable microphoneOffIcon;

    public final void setVoiceInputItem(final ExtendedFloatingActionButton voiceFab,
                                        final Drawable microphoneOnIcon,
                                        final Drawable microphoneOffIcon) {
        this.voiceFab = voiceFab;
        this.microphoneOnIcon = microphoneOnIcon;
        this.microphoneOffIcon = microphoneOffIcon;

        showNotListening();
        voiceFab.setOnClickListener(view -> tryToGetInput());
    }

    private void showListening() {
        voiceFab.setIcon(microphoneOnIcon);
        voiceFab.extend();
    }

    private void showNotListening() {
        voiceFab.setIcon(microphoneOffIcon);
        voiceFab.shrink();
    }

    /**
     * Listens for some spoken input from the microphone.
     * <br><br>
     * Overriding functions should report results to the
     * {@link InputDevice#notifyInputReceived(String)} and
     * {@link InputDevice#notifyError(Throwable)} functions, and they must call
     * {@link #onStartedListening()} when they turn on the microphone and
     * {@link #onFinishedListening()} when instead they turn it off.
     */
    @Override
    public abstract void tryToGetInput();

    /**
     * This must be called by functions overriding {@code startListening()} when
     * they have finished listening, so that the microphone icon can be turned off.
     */
    protected final void onStartedListening() {
        showListening();
    }

    /**
     * This must be called by functions overriding {@code startListening()} when
     * they have finished listening, so that the microphone icon can be turned off.
     */
    protected final void onFinishedListening() {
        showNotListening();
    }
}
