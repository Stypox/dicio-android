package org.stypox.dicio.io.wake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WakeDevice {
    val state: StateFlow<WakeState>

    /**
     * The wake word device will emit an item here when it detects a wake word uttered by the user.
     * The flow has `replay = 0` to avoid previous detections from appearing later.
     */
    val wakeWordTriggered: Flow<Unit>

    fun download()

    /**
     * Should be called only from the background service.
     */
    suspend fun loadAndListen()
}
