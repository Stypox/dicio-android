package com.dicio.dicio_android.io.input;

import androidx.annotation.Nullable;

public abstract class InputDevice {
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

    public final void setOnInputReceivedListener(@Nullable OnInputReceivedListener listener) {
        this.onInputReceivedListener = listener;
    }

    /**
     * This has to be called by functions overriding {@code tryToGetInput()}
     * when some input from the user is received.
     * @param input the (raw) received input
     */
    protected void notifyInputReceived(String input) {
        if (onInputReceivedListener != null) {
            onInputReceivedListener.onInputReceived(input);
        }
    }

    /**
     * This has to be called by functions overriding {@code tryToGetInput()}
     * when the user sent some input, but it could not be processed due to
     * an error.
     * @param e an exception to handle
     */
    protected void notifyError(Throwable e) {
        if (onInputReceivedListener != null) {
            onInputReceivedListener.onError(e);
        }
    }
}
