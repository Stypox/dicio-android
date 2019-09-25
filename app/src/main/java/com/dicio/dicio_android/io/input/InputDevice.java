package com.dicio.dicio_android.io.input;

import androidx.annotation.Nullable;

public abstract class InputDevice {
    public interface OnInputReceivedListener {
        void onInputReceived(String input);
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
}
