package org.stypox.dicio.screenshot

import kotlinx.coroutines.flow.MutableStateFlow
import org.stypox.dicio.di.SttInputDeviceWrapper
import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.io.input.SttState

class FakeSttInputDeviceWrapper : SttInputDeviceWrapper {
    override val uiState: MutableStateFlow<SttState> = MutableStateFlow(SttState.NotInitialized)

    override fun tryLoad(thenStartListeningEventListener: ((InputEvent) -> Unit)?): Boolean {
        return true
    }

    override fun stopListening() {
    }

    override fun onClick(eventListener: (InputEvent) -> Unit) {
    }

    override fun releaseResources() {
    }
}
