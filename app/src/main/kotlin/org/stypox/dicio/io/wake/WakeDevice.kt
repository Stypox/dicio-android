package org.stypox.dicio.io.wake

import kotlinx.coroutines.flow.StateFlow

interface WakeDevice {
    val state: StateFlow<WakeState>

    fun download()

    /**
     * This is blocking and should be called only from the background service.
     */
    fun processFrame(audio16bitPcm: ShortArray): Float

    /**
     * The size of audio frames passed to [processFrame]
     */
    fun frameSize(): Int

    fun destroy()
}
