package org.stypox.dicio.io.wake.oww

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.stypox.dicio.io.wake.WakeDevice
import org.stypox.dicio.io.wake.WakeState

class OpenWakeWordDevice : WakeDevice {
    override val state: StateFlow<WakeState>
        get() = TODO("Not yet implemented")
    override val wakeWordTriggered: Flow<Unit>
        get() = TODO("Not yet implemented")

    override fun download() {
        TODO("Not yet implemented")
    }

    override suspend fun loadAndListen() {
        TODO("Not yet implemented")
    }
}
