package org.stypox.dicio.io.wake.oww

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.stypox.dicio.io.wake.WakeDevice
import org.stypox.dicio.io.wake.WakeState

class OpenWakeWordDevice : WakeDevice {
    private val _state: MutableStateFlow<WakeState> = MutableStateFlow(WakeState.Loading)
    override val state: StateFlow<WakeState> = _state

    private val _wakeWordTriggered: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 0)
    override val wakeWordTriggered: Flow<Unit> = _wakeWordTriggered

    override fun download() {
        TODO("Not yet implemented")
    }

    override suspend fun loadAndListen() {
        TODO("Not yet implemented")
    }
}
