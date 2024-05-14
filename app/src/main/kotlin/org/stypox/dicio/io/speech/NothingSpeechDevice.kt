package org.stypox.dicio.io.speech

class NothingSpeechDevice : InstantSpeechDevice() {
    override fun speak(speechOutput: String) {
        // do nothing
    }

    override fun stopSpeaking() {}
    override fun cleanup() {}
}
