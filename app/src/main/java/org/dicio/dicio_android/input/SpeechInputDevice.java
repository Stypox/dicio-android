package org.dicio.dicio_android.input;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.dicio.dicio_android.R;

public abstract class SpeechInputDevice extends InputDevice {

    @Nullable private ExtendedFloatingActionButton voiceFab = null;
    @DrawableRes private int currentDrawable;
    private boolean currentShowExtendedFab;

    public SpeechInputDevice() {
        onLoading(); // start with loading state
    }


    /**
     * Attach this {@link SpeechInputDevice} to the {@link ExtendedFloatingActionButton} it should
     * use to show loading, inactive and listening states. Provide a {@code null} fab to detach the
     * current one.
     * @param voiceFab the fab, which should an empty string set as text so that the first time it
     *                 is extended everything is handled correctly.
     */
    public final void setVoiceFab(@Nullable final ExtendedFloatingActionButton voiceFab) {
        if (this.voiceFab != null) {
            // release previous on click listener to allow garbage collection to kick in
            this.voiceFab.setOnClickListener(null);
        }

        this.voiceFab = voiceFab;

        if (voiceFab != null) {
            voiceFab.setText(voiceFab.getContext().getString(R.string.listening));
            showState(currentDrawable, currentShowExtendedFab);
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
     * functions, and they must call {@link #onStartedListening()} when they turn on the microphone
     * and {@link #onInactive()} when instead they turn it off.
     */
    @Override
    public abstract void tryToGetInput();


    /**
     * This must be called by functions overriding {@link #tryToGetInput()} when they have started
     * listening, so that the microphone on icon can be shown.
     */
    protected final void onLoading() {
        showState(R.drawable.ic_refresh_white, false);
    }

    /**
     * This must be called by functions overriding {@link #tryToGetInput()} when they have finished
     * listening or by functions overriding {@link #load()} when they have finished loading, so that
     * the so that the microphone off icon can be shown.
     */
    protected final void onInactive() {
        showState(R.drawable.ic_mic_none_white, false);
    }

    /**
     * This must be called by functions overriding {@link #tryToGetInput()} when they have started
     * listening, so that the microphone on icon can be shown.
     */
    protected final void onStartedListening() {
        showState(R.drawable.ic_mic_white, true);
    }


    private void showState(@DrawableRes final int drawable, final boolean showExtendedFab) {
        currentDrawable = drawable;
        currentShowExtendedFab = showExtendedFab;
        if (voiceFab != null) {
            voiceFab.setIcon(
                    AppCompatResources.getDrawable(voiceFab.getContext(), currentDrawable));
            if (currentShowExtendedFab) {
                voiceFab.extend();
            } else {
                voiceFab.shrink();
            }
        }
    }
}
