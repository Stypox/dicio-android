package com.dicio.dicio_android.input;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;

public abstract class SpeechInputDevice extends InputDevice {
    private MenuItem voiceInputItem;
    private Drawable microphoneOnIcon;
    private Drawable microphoneOffIcon;

    public final void setVoiceInputItem(MenuItem voiceInputItem, Drawable microphoneOnIcon, Drawable microphoneOffIcon) {
        this.voiceInputItem = voiceInputItem;
        this.microphoneOnIcon = microphoneOnIcon;
        this.microphoneOffIcon = microphoneOffIcon;

        voiceInputItem.setIcon(microphoneOffIcon);
        voiceInputItem.setOnMenuItemClickListener(item -> {
            tryToGetInput();
            return false;
        });
    }

    @Override
    public final void tryToGetInput() {
        voiceInputItem.setIcon(microphoneOnIcon);
        startListening();
    }

    /**
     * Listens for some spoken input from the microphone.
     * <br><br>
     * Overriding functions should report results to the
     * {@code notifyInputReceived()} and {@code notifyError()} functions,
     * and they must call {@code onFinishedListening} when they have
     * finished listening.
     */
    public abstract void startListening();

    /**
     * This must be called by functions overriding {@code startListening()} when
     * they have finished listening, so that the microphone icon can be turned off.
     */
    protected final void onFinishedListening() {
        voiceInputItem.setIcon(microphoneOffIcon);
    }
}
