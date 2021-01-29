package org.dicio.dicio_android.input;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.dicio.dicio_android.R;

public abstract class SpeechInputDevice extends InputDevice {

    public static final class UnableToAccessMicrophoneException extends Exception {
        UnableToAccessMicrophoneException() {
            super("Unable to access microphone."
                    + " Microphone might be already in use or the permission was not granted.");
        }
    }


    private static final int LOADING = 0, INACTIVE = 1, LISTENING = 2;

    @Nullable private ExtendedFloatingActionButton voiceFab = null;
    @Nullable private ProgressBar voiceLoading = null;

    private int currentState = INACTIVE; // start with inactive state


    /**
     * Attach this {@link SpeechInputDevice} to the {@link ExtendedFloatingActionButton} it should
     * use to show loading, inactive and listening states. Provide a {@code null} fab to detach the
     * current one.
     * @param voiceFab the fab, which should an empty string set as text so that the first time it
     *                 is extended everything is handled correctly.
     */
    public final void setVoiceViews(@Nullable final ExtendedFloatingActionButton voiceFab,
                                    @Nullable final ProgressBar voiceLoading) {
        if (this.voiceFab != null) {
            // release previous on click listener to allow garbage collection to kick in
            this.voiceFab.setOnClickListener(null);
        }

        this.voiceFab = voiceFab;
        this.voiceLoading = voiceLoading;

        if (voiceFab != null) {
            voiceFab.setText(voiceFab.getContext().getString(R.string.listening));
            showState(currentState);
            voiceFab.setOnClickListener(view -> tryToGetInput());
        }
    }


    /**
     * Prepares the speech recognizer. If doing heavy work, run it in an asynchronous thread.
     * <br><br>
     * Overriding functions must call {@link #onLoading()} when they start loading and {@link
     * #onInactive()} when instead they have finished loading. Errors should be reported to {@link
     * #notifyError(Throwable)}. Note that the starting icon for a {@link SpeechInputDevice} is
     * already the loading indicator.
     */
    @Override
    public abstract void load();

    /**
     * Listens for some spoken input from the microphone. Should run in an asynchronous thread.
     * <br><br>
     * Overriding functions should report results to the {@link
     * InputDevice#notifyInputReceived(String)} and {@link InputDevice#notifyError(Throwable)}
     * functions, and they must call {@link #onListening()} when they turn on the microphone
     * and {@link #onInactive()} when instead they turn it off.
     */
    @Override
    public abstract void tryToGetInput();


    /**
     * This must be called by functions overriding {@link #tryToGetInput()} when they have started
     * listening, so that the microphone on icon can be shown.
     */
    protected final void onLoading() {
        showState(LOADING);
    }

    /**
     * This must be called by functions overriding {@link #tryToGetInput()} when they have finished
     * listening or by functions overriding {@link #load()} when they have finished loading, so that
     * the so that the microphone off icon can be shown.
     */
    protected final void onInactive() {
        showState(INACTIVE);
    }

    /**
     * This must be called by functions overriding {@link #tryToGetInput()} when they have started
     * listening, so that the microphone on icon can be shown.
     */
    protected final void onListening() {
        showState(LISTENING);
    }


    private void showState(final int state) {
        currentState = state;
        if (voiceFab != null && voiceLoading != null) {
            switch (state) {
                case LOADING:
                    voiceFab.setIcon(new ColorDrawable(Color.TRANSPARENT));
                    voiceFab.shrink();
                    voiceLoading.setVisibility(View.VISIBLE);
                    break;
                case INACTIVE: default:
                    voiceFab.setIcon(AppCompatResources.getDrawable(voiceFab.getContext(),
                            R.drawable.ic_mic_none_white));
                    voiceFab.shrink();
                    voiceLoading.setVisibility(View.GONE);
                    break;
                case LISTENING:
                    voiceFab.setIcon(AppCompatResources.getDrawable(voiceFab.getContext(),
                            R.drawable.ic_mic_white));
                    voiceFab.extend();
                    voiceLoading.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
