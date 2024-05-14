package org.stypox.dicio.io.speech

import android.view.View
import com.google.android.material.snackbar.Snackbar

class SnackbarSpeechDevice(private var view: View) : InstantSpeechDevice() {
    private var currentSnackbar: Snackbar? = null
    override fun speak(speechOutput: String) {
        currentSnackbar = Snackbar.make(view, speechOutput, Snackbar.LENGTH_LONG).apply {
            show()
        }
    }

    override fun stopSpeaking() {
        currentSnackbar?.dismiss()
    }

    override fun cleanup() {
        currentSnackbar?.apply {
            dismiss()
            currentSnackbar = null
        }
    }
}
