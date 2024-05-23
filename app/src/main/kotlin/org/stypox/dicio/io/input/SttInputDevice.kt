package org.stypox.dicio.io.input

import kotlinx.coroutines.flow.StateFlow
import org.stypox.dicio.ui.home.SttState

interface SttInputDevice {
    val uiState: StateFlow<SttState>

    fun tryLoad(thenStartListening: Boolean)

    fun onClick()
}
