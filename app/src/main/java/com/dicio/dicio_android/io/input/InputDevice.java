package com.dicio.dicio_android.io.input;

import androidx.annotation.Nullable;

public abstract class InputDevice {
    public interface OnInputReceivedListener {
        void onInputReceived(String input);
        void onError(Throwable e);
    }

    @Nullable
    private OnInputReceivedListener onInputReceivedListener = null;

    public abstract void startListening();

    public final void setOnInputReceivedListener(@Nullable OnInputReceivedListener listener) {
        this.onInputReceivedListener = listener;
    }

    protected void notifyInputReceived(String input) {
        if (onInputReceivedListener != null) {
            onInputReceivedListener.onInputReceived(input);
        }
    }

    protected void notifyError(Throwable e) {
        if (onInputReceivedListener != null) {
            onInputReceivedListener.onError(e);
        }
    }
}
