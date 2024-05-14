package org.stypox.dicio.io.input

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.stypox.dicio.R

abstract class SpeechInputDevice : InputDevice() {
    class UnableToAccessMicrophoneException internal constructor() : Exception(
        "Unable to access microphone."
                + " Microphone might be already in use or the permission was not granted."
    )

    private enum class ShownState {
        REQUIRES_DOWNLOAD, LOADING, INACTIVE, LISTENING
    }

    private var voiceFab: ExtendedFloatingActionButton? = null
    private var voiceLoading: ProgressBar? = null
    private var currentShownState = ShownState.INACTIVE // start with inactive state

    /**
     * Attach this [SpeechInputDevice] to the [ExtendedFloatingActionButton] it should
     * use to show loading, inactive and listening states. Provide a `null` fab to detach the
     * current one.
     * @param voiceFabToSet the fab, which should have an empty string set as text so that the first
     * time it is extended everything is handled correctly.
     */
    fun setVoiceViews(
        voiceFabToSet: ExtendedFloatingActionButton?,
        voiceLoadingToSet: ProgressBar?
    ) {
        // release previous on click listener to allow garbage collection to kick in
        voiceFab?.setOnClickListener(null)

        voiceFab = voiceFabToSet
        voiceLoading = voiceLoadingToSet
        if (voiceFabToSet != null) {
            voiceFabToSet.text = voiceFabToSet.context.getString(R.string.listening)
            showState(currentShownState)
            voiceFabToSet.setOnClickListener(View.OnClickListener {
                if (currentShownState == ShownState.LISTENING) {
                    // already listening, so stop listening
                    cancelGettingInput()
                } else {
                    tryToGetInput(true)
                }
            })
        }
    }

    override fun cleanup() {
        super.cleanup()
        setVoiceViews(null, null)
        currentShownState = ShownState.INACTIVE
    }

    /**
     * Prepares the speech recognizer. If doing heavy work, run it in an asynchronous thread.
     * <br></br><br></br>
     * Overriding functions must call [onLoading] when they start loading and [onInactive] when instead they have finished loading. Errors should be reported to [notifyError]. Note that the starting icon for a [SpeechInputDevice] is
     * already the loading indicator.
     */
    abstract override fun load()

    /**
     * Listens for some spoken input from the microphone. Should run in an asynchronous thread.
     * <br></br><br></br>
     * Overriding functions should report partial results to [notifyPartialInputReceived], final
     * results to [notifyInputReceived] or [notifyNoInputReceived] (based on whether some input was
     * received or not) and errors to [notifyError]. They must call [onListening] when they turn on
     * the microphone and [onInactive] when instead they turn it off.
     *
     * @param manual true if and only if the user manually pressed on the specific button that
     * activates this input device, false otherwise. This might be useful to prevent
     * e.g. voice model downloads from starting in case the user didn't explicitly
     * trigger the input device.
     */
    @Suppress("RedundantOverride")
    override fun tryToGetInput(manual: Boolean) {
        super.tryToGetInput(manual) // overridden just to provide a more detailed documentation ^
    }

    /**
     * Stops listening and turns off the microphone after [tryToGetInput] was
     * called. Should do nothing if called while not listening. Any partial result is discarded.
     * Called for example when the user leaves the app.
     * <br></br><br></br>
     * Overriding functions should call [notifyNoInputReceived] and [onInactive]
     * when they turn off the microphone.
     */
    abstract override fun cancelGettingInput()

    /**
     * This must be called by functions overriding [tryToGetInput] if the `manual` parameter is `false` and loading the voice model would require downloading
     * files from the internet. It must also be called by [load] when loading the voice
     * model would require downloading files from the internet (since [load] is never
     * called after a user action but automatically, which is equivalent to having `manual=false` for [tryToGetInput]). A download icon will be shown.
     */
    protected fun onRequiresDownload() {
        showState(ShownState.REQUIRES_DOWNLOAD)
    }

    /**
     * This must be called by functions overriding [tryToGetInput] when they have
     * started listening, so that the microphone on icon can be shown.
     */
    protected fun onLoading() {
        showState(ShownState.LOADING)
    }

    /**
     * This must be called by functions overriding [tryToGetInput] when they have
     * finished listening or by functions overriding [load] when they have finished
     * loading, so that the so that the microphone off icon can be shown.
     */
    protected fun onInactive() {
        showState(ShownState.INACTIVE)
    }

    /**
     * This must be called by functions overriding [tryToGetInput] when they have
     * started listening, so that the microphone on icon can be shown.
     */
    protected fun onListening() {
        showState(ShownState.LISTENING)
    }

    private fun showState(state: ShownState) {
        currentShownState = state
        val voiceFab = voiceFab ?: return
        val voiceLoading = voiceLoading ?: return

        when (state) {
            ShownState.REQUIRES_DOWNLOAD -> {
                voiceFab.icon = AppCompatResources.getDrawable(
                    voiceFab.context,
                    R.drawable.ic_download_white
                )
                voiceFab.shrink()
                voiceLoading.visibility = View.GONE
            }
            ShownState.LOADING -> {
                voiceFab.icon = ColorDrawable(Color.TRANSPARENT)
                voiceFab.shrink()
                voiceLoading.visibility = View.VISIBLE
            }
            ShownState.INACTIVE -> {
                voiceFab.icon = AppCompatResources.getDrawable(
                    voiceFab.context,
                    R.drawable.ic_mic_none_white
                )
                voiceFab.shrink()
                voiceLoading.visibility = View.GONE
            }
            ShownState.LISTENING -> {
                voiceFab.icon = AppCompatResources.getDrawable(
                    voiceFab.context,
                    R.drawable.ic_mic_white
                )
                voiceFab.extend()
                voiceLoading.visibility = View.GONE
            }
        }
    }
}
