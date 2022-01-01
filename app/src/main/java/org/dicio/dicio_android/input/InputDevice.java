package org.dicio.dicio_android.input;

import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import org.dicio.dicio_android.BuildConfig;
import org.dicio.skill.util.CleanableUp;

import java.util.Collections;
import java.util.List;

public abstract class InputDevice implements CleanableUp {

    /**
     * Used to provide the input from the user to whatever code uses it
     */
    public interface InputDeviceListener {

        /**
         * Called right at the beginning of {@link #tryToGetInput(boolean)} to notify that the
         * device is trying to get some input (useful for stopping other input or output devices)
         */
        void onTryingToGetInput();

        /**
         * Called when the user provided some partial input (e.g. while talking)
         * @param input the received partial input
         */
        void onPartialInputReceived(String input);

        /**
         * Called when some input was received from the user
         * Sometimes input devices can return multiple alternative outputs with different confidences. If the first input in the List doesn't fit to any skill the next input will be tried.
         * @param input the list of alternative (raw) inputs, sorted by confidence (the most confident item is the first one). Use Collections.singletonlist() if there's only one input.
         */
        void onInputReceived(List<String> input);


        /**
         * Called when no input was received from the user after he seemed to want to provide some
         */
        void onNoInputReceived();

        /**
         * Called when an error occurs while trying to get input or processing it
         * @param e the exception
         */
        void onError(Throwable e);
    }

    private static final String TAG = InputDevice.class.getSimpleName();

    @Nullable
    private InputDeviceListener inputDeviceListener = null;


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
     * #notifyPartialInputReceived(String)}, final results to {@link #notifyInputReceived(String)}
     * or {@link #notifyNoInputReceived()} (based on whether some input was received or not) and
     * errors to {@link #notifyError(Throwable)}.
     *
     * @param manual true if and only if the user manually pressed on the specific button that
     *               activates this input device, false otherwise. This might be useful to prevent
     *               e.g. voice model downloads from starting in case the user didn't explicitly
     *               trigger the input device.
     */
    @CallSuper
    public void tryToGetInput(boolean manual) {
        if (inputDeviceListener != null) {
            inputDeviceListener.onTryingToGetInput();
        }
    }

    /**
     * Cancels any input being received after {@link #tryToGetInput(boolean)} was called. Should do
     * nothing if called while not getting input. Any partial input is discarded. Called for example
     * when the user leaves the app.
     * <br><br>
     * Overriding functions should call {@link #notifyNoInputReceived()} when they stop getting
     * input.
     */
    public abstract void cancelGettingInput();

    /**
     * Sets the listener, used to provide the input from the user to whatever code uses it
     * @param listener the listener to use, set it to {@code null} to remove it
     */
    public final void setInputDeviceListener(@Nullable final InputDeviceListener listener) {
        this.inputDeviceListener = listener;
    }

    @Override
    public void cleanup() {
        setInputDeviceListener(null);
    }


    /**
     * This has to be called by functions overriding {@link #tryToGetInput(boolean)} when some input
     * from the user is received. Can be called multiple times while getting input.
     * @param input the (raw) received input
     */
    protected void notifyPartialInputReceived(final String input) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Partial input from user: " + input);
        }

        if (inputDeviceListener != null) {
            inputDeviceListener.onPartialInputReceived(input);
        }
    }

    /**
     * This has to be called by functions overriding {@link #tryToGetInput(boolean)} when some input
     * from the user is received. Should be called only once, when stopping to get input. Should not
     * be called if {@link #notifyNoInputReceived()} is called instead.
     * @param input the (raw) received input
     */
    protected void notifyInputReceived(final String input) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input from user: " + input);
        }

        if (inputDeviceListener != null) {
            inputDeviceListener.onInputReceived(Collections.singletonList(input));
        }
    }
    /**
     * This has to be called by functions overriding {@link #tryToGetInput(boolean)} when some input
     * from the user is received. Should be called only once, when stopping to get input. Should not
     * be called if {@link #notifyNoInputReceived()} is called instead.
     * Vosk can return multiple outputs with diffrent propability. If the first input doesn't fit to any skill the next input will be tried.
     * @param input sorted list of (raw) inputs, the most confident input must be the first item in the list.
     */
    protected void notifyInputReceived(final List<String> input) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input from user: " + input.toString());
        }

        if (inputDeviceListener != null) {
            inputDeviceListener.onInputReceived(input);
        }
    }

    /**
     * This has to be called by functions overriding {@link #tryToGetInput(boolean)} when the user
     * provided no input. Should be called only once, when stopping to get input. Should not be
     * called if {@link #notifyInputReceived(String)} is called instead.
     */
    protected void notifyNoInputReceived() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "No input from user");
        }

        if (inputDeviceListener != null) {
            inputDeviceListener.onNoInputReceived();
        }
    }

    /**
     * This has to be called by functions overriding {@link #tryToGetInput(boolean)} when the user
     * sent some input, but it could not be processed due to an error. This can also be called if
     * there was an error while loading, and can be called by functions overriding {@link #load()}.
     * @param e an exception to handle
     */
    protected void notifyError(final Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input error: " + e.getMessage(), e);
        }

        if (inputDeviceListener != null) {
            inputDeviceListener.onError(e);
        }
    }
}
