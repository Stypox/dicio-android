package org.stypox.dicio.io.speech

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is always instantiated, but will do nothing if
 * it is not the speech device chosen by the user
 */
@Singleton
class SnackbarSpeechDevice @Inject constructor() : InstantSpeechDevice() {
    // null indicates that any snackbar should be dismissed
    private val _events = MutableSharedFlow<String?>(0, 1, BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<String?> = _events

    override fun speak(speechOutput: String) {
        _events.tryEmit(speechOutput)
    }

    override fun stopSpeaking() {
        _events.tryEmit(null)
    }

    override fun cleanup() {
        // nothing to do
    }
}
