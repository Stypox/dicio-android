package org.stypox.dicio.screenshot

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.stypox.dicio.di.WakeDeviceWrapper
import org.stypox.dicio.io.wake.WakeState

class FakeWakeDeviceWrapper : WakeDeviceWrapper {
    override val state: StateFlow<WakeState?> = MutableStateFlow(null)

    override fun download() {}

    override fun processFrame(audio16bitPcm: ShortArray): Boolean = false

    override fun frameSize(): Int = 1312

    override fun reinitializeToReleaseResources() {}
}
