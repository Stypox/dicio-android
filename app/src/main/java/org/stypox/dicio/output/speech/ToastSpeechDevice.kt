package org.stypox.dicio.output.speech

import android.content.Context
import android.widget.Toast

class ToastSpeechDevice(private var context: Context) : InstantSpeechDevice() {
    private var currentToast: Toast? = null
    override fun speak(speechOutput: String) {
        currentToast = Toast.makeText(context, speechOutput, Toast.LENGTH_LONG).apply {
            show()
        }
    }

    override fun stopSpeaking() {
        currentToast?.cancel()
    }

    override fun cleanup() {
        currentToast?.apply {
            cancel()
            currentToast = null
        }
    }
}