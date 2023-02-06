package org.stypox.dicio.input

import android.util.Log
import androidx.annotation.CallSuper
import org.dicio.skill.util.CleanableUp
import org.stypox.dicio.BuildConfig

abstract class InputDevice : CleanableUp {
    /**
     * Used to provide the input from the user to whatever code uses it
     */
    interface InputDeviceListener {
        /**
         * Called right at the beginning of [tryToGetInput] to notify that the
         * device is trying to get some input (useful for stopping other input or output devices)
         */
        fun onTryingToGetInput()

        /**
         * Called when the user provided some partial input (e.g. while talking)
         * @param input the received partial input
         */
        fun onPartialInputReceived(input: String)

        /**
         * Called when some input was received from the user. Sometimes input devices can return
         * multiple alternative outputs with different confidences. The alternatives should be
         * sorted by confidence, since they will be tried in order and the first one that matches a
         * skill will be kept.
         *
         * @param input the list of alternative (raw) inputs, sorted by confidence (the most
         * confident item is the first one). Use [listOf] if there's only one input.
         */
        fun onInputReceived(input: List<String>)

        /**
         * Called when no input was received from the user after he seemed to want to provide some
         */
        fun onNoInputReceived()

        /**
         * Called when an error occurs while trying to get input or processing it
         * @param e the exception
         */
        fun onError(e: Throwable)
    }

    private var inputDeviceListener: InputDeviceListener? = null

    /**
     * Prepares the input device. If doing heavy work, run it in an asynchronous thread.
     * <br></br><br></br>
     * Overriding functions should report errors to [notifyError].
     */
    abstract fun load()

    /**
     * Tries to get input in any way. Should run in an asynchronous thread.
     * <br></br><br></br>
     * Overriding functions should report partial results to [notifyPartialInputReceived], final
     * results to [notifyInputReceived] or [notifyNoInputReceived] (based on whether some input was
     * received or not) and errors to [notifyError].
     *
     * @param manual true if and only if the user manually pressed on the specific button that
     * activates this input device, false otherwise. This might be useful to prevent e.g. voice
     * model downloads from starting in case the user didn't explicitly trigger the input device.
     */
    @CallSuper
    open fun tryToGetInput(manual: Boolean) {
        inputDeviceListener?.onTryingToGetInput()
    }

    /**
     * Cancels any input being received after [tryToGetInput] was called. Should do
     * nothing if called while not getting input. Any partial input is discarded. Called for example
     * when the user leaves the app.
     * <br></br><br></br>
     * Overriding functions should call [notifyNoInputReceived] when they stop getting
     * input.
     */
    abstract fun cancelGettingInput()

    /**
     * Sets the listener, used to provide the input from the user to whatever code uses it
     * @param listener the listener to use, set it to `null` to remove it
     */
    fun setInputDeviceListener(listener: InputDeviceListener?) {
        inputDeviceListener = listener
    }

    override fun cleanup() {
        setInputDeviceListener(null)
    }

    /**
     * This has to be called by functions overriding [tryToGetInput] when some input
     * from the user is received. Can be called multiple times while getting input.
     * @param input the (raw) received input
     */
    protected fun notifyPartialInputReceived(input: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Partial input from user: $input")
        }
        inputDeviceListener?.onPartialInputReceived(input)
    }

    /**
     * This has to be called by functions overriding [tryToGetInput] when some input
     * from the user is received. Should be called only once, when stopping to get input. Should not
     * be called if [notifyNoInputReceived] is called instead.
     * @param input the (raw) received input
     */
    protected fun notifyInputReceived(input: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input from user: $input")
        }
        inputDeviceListener?.onInputReceived(listOf(input))
    }

    /**
     * This has to be called by functions overriding [tryToGetInput] when some input
     * from the user is received. Should be called only once, when stopping to get input. Should not
     * be called if [notifyNoInputReceived] is called instead. Sometimes input devices can
     * return multiple alternative outputs with different confidences. The alternatives should be
     * sorted by confidence, since they will be tried in order and the first one that matches a
     * skill will be kept.
     *
     * @param input the list of alternative (raw) inputs, sorted by confidence (the most confident
     * item is the first one). Use [listOf] if there's only one input.
     */
    protected fun notifyInputReceived(input: List<String>) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input from user: $input")
        }
        inputDeviceListener?.onInputReceived(input)
    }

    /**
     * This has to be called by functions overriding [tryToGetInput] when the user
     * provided no input. Should be called only once, when stopping to get input. Should not be
     * called if [notifyInputReceived] is called instead.
     */
    protected fun notifyNoInputReceived() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "No input from user")
        }
        inputDeviceListener?.onNoInputReceived()
    }

    /**
     * This has to be called by functions overriding [tryToGetInput] when the user
     * sent some input, but it could not be processed due to an error. This can also be called if
     * there was an error while loading, and can be called by functions overriding [load].
     * @param e an exception to handle
     */
    protected fun notifyError(e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Input error: " + e.message, e)
        }
        inputDeviceListener?.onError(e)
    }

    companion object {
        private val TAG = InputDevice::class.java.simpleName
    }
}