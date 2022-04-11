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


    private enum ShownState {
        REQUIRES_DOWNLOAD, LOADING, INACTIVE, LISTENING
    }

    @Nullable private ExtendedFloatingActionButton voiceFab = null;
    @Nullable private ProgressBar voiceLoading = null;

    private ShownState currentShownState = ShownState.INACTIVE; // start with inactive state


    /**
     * Attach this {@link SpeechInputDevice} to the {@link ExtendedFloatingActionButton} it should
     * use to show loading, inactive and listening states. Provide a {@code null} fab to detach the
     * current one.
     * @param voiceFabToSet the fab, which should have an empty string set as text so that the first
     *                      time it is extended everything is handled correctly.
     */
    public final void setVoiceViews(@Nullable final ExtendedFloatingActionButton voiceFabToSet,
                                    @Nullable final ProgressBar voiceLoadingToSet) {
        if (this.voiceFab != null) {
            // release previous on click listener to allow garbage collection to kick in
            this.voiceFab.setOnClickListener(null);
        }

        this.voiceFab = voiceFabToSet;
        this.voiceLoading = voiceLoadingToSet;

        if (voiceFabToSet != null) {
            voiceFabToSet.setText(voiceFabToSet.getContext().getString(R.string.listening));
            showState(currentShownState);
            voiceFabToSet.setOnClickListener(view -> {
                if (currentShownState == ShownState.LISTENING) {
                    // already listening, so stop listening
                    cancelGettingInput();
                } else {
                    tryToGetInput(true);
                }
            });
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        setVoiceViews(null, null);
        currentShownState = ShownState.INACTIVE;
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
     * Overriding functions should report partial results to {@link
     * #notifyPartialInputReceived(String)}, final results to {@link #notifyInputReceived(String)}
     * or {@link #notifyNoInputReceived()} (based on whether some input was received or not) and
     * errors to {@link #notifyError(Throwable)}. They must call {@link #onListening()} when they
     * turn on the microphone and {@link #onInactive()} when instead they turn it off.
     *
     * @param manual true if and only if the user manually pressed on the specific button that
     *               activates this input device, false otherwise. This might be useful to prevent
     *               e.g. voice model downloads from starting in case the user didn't explicitly
     *               trigger the input device.
     */
    @Override
    public void tryToGetInput(final boolean manual) {
        super.tryToGetInput(manual); // overridden just to provide a more detailed documentation ^
    }

    /**
     * Stops listening and turns off the microphone after {@link #tryToGetInput(boolean)} was
     * called. Should do nothing if called while not listening. Any partial result is discarded.
     * Called for example when the user leaves the app.
     * <br><br>
     * Overriding functions should call {@link #notifyNoInputReceived()} and {@link #onInactive()}
     * when they turn off the microphone.
     */
    @Override
    public abstract void cancelGettingInput();

    /**
     * This must be called by functions overriding {@link #tryToGetInput(boolean)} if the {@code
     * manual} parameter is {@code false} and loading the voice model would require downloading
     * files from the internet. It must also be called by {@link #load()} when loading the voice
     * model would require downloading files from the internet (since {@link #load()} is never
     * called after a user action but automatically, which is equivalent to having {@code
     * manual=false} for {@link #tryToGetInput(boolean)}). A download icon will be shown.
     */
    protected final void onRequiresDownload() {
        showState(ShownState.REQUIRES_DOWNLOAD);
    }

    /**
     * This must be called by functions overriding {@link #tryToGetInput(boolean)} when they have
     * started listening, so that the microphone on icon can be shown.
     */
    protected final void onLoading() {
        showState(ShownState.LOADING);
    }

    /**
     * This must be called by functions overriding {@link #tryToGetInput(boolean)} when they have
     * finished listening or by functions overriding {@link #load()} when they have finished
     * loading, so that the so that the microphone off icon can be shown.
     */
    protected final void onInactive() {
        showState(ShownState.INACTIVE);
    }

    /**
     * This must be called by functions overriding {@link #tryToGetInput(boolean)} when they have
     * started listening, so that the microphone on icon can be shown.
     */
    protected final void onListening() {
        showState(ShownState.LISTENING);
    }


    private void showState(final ShownState state) {
        currentShownState = state;
        if (voiceFab != null && voiceLoading != null) {
            switch (state) {
                case REQUIRES_DOWNLOAD:
                    voiceFab.setIcon(AppCompatResources.getDrawable(voiceFab.getContext(),
                            R.drawable.ic_download_white));
                    voiceFab.shrink();
                    voiceLoading.setVisibility(View.GONE);
                    break;
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
