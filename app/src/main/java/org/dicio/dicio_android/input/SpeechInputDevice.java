package org.dicio.dicio_android.input;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;

public abstract class SpeechInputDevice extends InputDevice {
    private MenuItem voiceInputItem;
    private Drawable microphoneOnIcon;
    private Drawable microphoneOffIcon;

    public final void setVoiceInputItem(final MenuItem voiceInputItem,
                                        final Drawable microphoneOnIcon,
                                        final Drawable microphoneOffIcon) {
        this.voiceInputItem = voiceInputItem;
        this.microphoneOnIcon = microphoneOnIcon;
        this.microphoneOffIcon = microphoneOffIcon;

        voiceInputItem.setIcon(microphoneOffIcon);
        voiceInputItem.setOnMenuItemClickListener(item -> {
            tryToGetInput();
            return false;
        });
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
        voiceInputItem.setIcon(microphoneOnIcon);
    }

    /**
     * This must be called by functions overriding {@code startListening()} when
     * they have finished listening, so that the microphone icon can be turned off.
     */
    protected final void onFinishedListening() {
        voiceInputItem.setIcon(microphoneOffIcon);
    }
}
