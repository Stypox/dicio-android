package org.dicio.dicio_android.input;

import android.util.Log;

import androidx.annotation.Nullable;

import org.dicio.dicio_android.BuildConfig;

public abstract class InputDevice {

    private static final String TAG = InputDevice.class.getSimpleName();

    public interface OnInputReceivedListener {
        void onInputReceived(String input);
        void onError(Throwable e);
    }

    @Nullable
    private OnInputReceivedListener onInputReceivedListener = null;

    /**
     * Tries to get input in any way (even asyncronically, if needed).
     * <br><br>
     * Overriding functions should report results to the
     * {@code notifyInputReceived()} and {@code notifyError()} functions.
     */
    public abstract void tryToGetInput();

    public final void setOnInputReceivedListener(@Nullable final OnInputReceivedListener listener) {
        this.onInputReceivedListener = listener;
    }

    /**
     * This has to be called by functions overriding {@code tryToGetInput()}
     * when some input from the user is received.
     * @param input the (raw) received input
     */
    protected void notifyInputReceived(final String input) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input from user: " + input);
        }

        if (onInputReceivedListener != null) {
            onInputReceivedListener.onInputReceived(input);
        }
    }

    // TODO add partialInputReceived()

    /**
     * This has to be called by functions overriding {@code tryToGetInput()}
     * when the user sent some input, but it could not be processed due to
     * an error.
     * @param e an exception to handle
     */
    protected void notifyError(final Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input error: " + e.getMessage(), e);
        }

        if (onInputReceivedListener != null) {
            onInputReceivedListener.onError(e);
        }
    }
}
