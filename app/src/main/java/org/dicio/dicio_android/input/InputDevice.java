package org.dicio.dicio_android.input;

import android.util.Log;

import androidx.annotation.Nullable;

import org.dicio.dicio_android.BuildConfig;

public abstract class InputDevice {

    /**
     * Used to provide the input from the user to whatever code uses it
     */
    public interface OnInputReceivedListener {

        /**
         * Called when the user provided some partial input (e.g. while talking)
         * @param input the received partial input
         */
        void onPartialInputReceived(String input);

        /**
         * Called when some input was received from the user
         * @param input the received input
         */
        void onInputReceived(String input);

        /**
         * Called when an error occurs while trying to get input or processing it
         * @param e the exception
         */
        void onError(Throwable e);
    }

    private static final String TAG = InputDevice.class.getSimpleName();

    @Nullable
    private OnInputReceivedListener onInputReceivedListener = null;


    /**
     * Prepares the input device. If doing heavy work, run it in an asynchronous thread.
     * <br><br>
     * Overriding functions should report errors to {@link #notifyError(Throwable)}.
     */
    public abstract void load();

    /**
     * Tries to get input in any way. Should run in an asynchronous thread.
     * <br><br>
     * Overriding functions should report partial results to {@link
     * #notifyPartialInputReceived(String)} results to {@link #notifyInputReceived(String)} and
     * errors to {@link #notifyError(Throwable)}.
     */
    public abstract void tryToGetInput();

    /**
     * Cancels any input being received after {@link #tryToGetInput()} was called. Should do nothing
     * if called while not getting input. Any partial input is discarded. Called for
     * example when the user leaves the app.
     */
    public abstract void cancelGettingInput();

    /**
     * Sets the listener, used to provide the input from the user to whatever code uses it
     * @param listener the listener to use, set it to {@code null} to remove it
     */
    public final void setOnInputReceivedListener(@Nullable final OnInputReceivedListener listener) {
        this.onInputReceivedListener = listener;
    }


    /**
     * This has to be called by functions overriding {@link #tryToGetInput()} when some input from
     * the user is received
     * @param input the (raw) received input
     */
    protected void notifyPartialInputReceived(final String input) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Partial input from user: " + input);
        }

        if (onInputReceivedListener != null) {
            onInputReceivedListener.onPartialInputReceived(input);
        }
    }

    /**
     * This has to be called by functions overriding {@link #tryToGetInput()} when some input from
     * the user is received
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

    /**
     * This has to be called by functions overriding {@link #tryToGetInput()} when the user sent
     * some input, but it could not be processed due to an error. This can also be called if there
     * was an error while loading, and can be called by functions overriding {@link #load()}.
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
