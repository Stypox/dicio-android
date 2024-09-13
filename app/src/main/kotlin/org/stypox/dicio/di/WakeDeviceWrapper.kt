package org.stypox.dicio.di

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.stypox.dicio.io.wake.WakeDevice
import org.stypox.dicio.io.wake.WakeState
import org.stypox.dicio.io.wake.oww.OpenWakeWordDevice
import org.stypox.dicio.settings.datastore.UserSettings
import org.stypox.dicio.settings.datastore.WakeDevice.*
import org.stypox.dicio.util.distinctUntilChangedBlockingFirst
import javax.inject.Singleton

interface WakeDeviceWrapper {
    val state: StateFlow<WakeState?>

    fun download()

    fun processFrame(audio16bitPcm: ShortArray): Float

    fun frameSize(): Int
}

typealias DataStoreWakeDevice = org.stypox.dicio.settings.datastore.WakeDevice

class WakeDeviceWrapperImpl(
    @ApplicationContext private val appContext: Context,
    dataStore: DataStore<UserSettings>,
    private val okHttpClient: OkHttpClient,
) : WakeDeviceWrapper {
    private val scope = CoroutineScope(Dispatchers.Default)

    private var wakeDevice: WakeDevice? = null

    // null means that the user has not enabled any STT input device
    private val _state: MutableStateFlow<WakeState?> = MutableStateFlow(null)
    override val state: StateFlow<WakeState?> = _state

    private var stateJob: Job? = null


    init {
        // Run blocking, because the data store is always available right away since LocaleManager
        // also initializes in a blocking way from the same data store.
        val (firstWakeDevice, nextWakeDeviceFlow) = dataStore.data
            .map { it.wakeDevice }
            .distinctUntilChangedBlockingFirst()

        wakeDevice = buildInputDevice(firstWakeDevice)
        scope.launch {
            restartUiStateJob()
        }

        scope.launch {
            nextWakeDeviceFlow.collect { setting ->
                val prevWakeDevice = wakeDevice
                wakeDevice = buildInputDevice(setting)
                prevWakeDevice?.destroy()
                restartUiStateJob()
            }
        }
    }

    private fun buildInputDevice(setting: DataStoreWakeDevice): WakeDevice? {
        return when (setting) {
            UNRECOGNIZED,
            WAKE_DEVICE_UNSET,
            WAKE_DEVICE_OWW -> OpenWakeWordDevice(appContext, okHttpClient)
            WAKE_DEVICE_NOTHING -> null
        }
    }

    private suspend fun restartUiStateJob() {
        stateJob?.cancel()
        val newWakeDevice = wakeDevice
        if (newWakeDevice == null) {
            stateJob = null
            _state.emit(null)
        } else {
            stateJob = scope.launch {
                newWakeDevice.state.collect { _state.emit(it) }
            }
        }
    }

    override fun download() {
        wakeDevice?.download()
    }

    override fun processFrame(audio16bitPcm: ShortArray): Float {
        val device = wakeDevice ?: throw IllegalArgumentException("No wake word device is enabled")
        return device.processFrame(audio16bitPcm)
    }

    override fun frameSize(): Int {
        return wakeDevice?.frameSize() ?: 0
    }
}

@Module
@InstallIn(SingletonComponent::class)
class WakeDeviceWrapperModule {
    @Provides
    @Singleton
    fun provideWakeDeviceWrapper(
        @ApplicationContext appContext: Context,
        dataStore: DataStore<UserSettings>,
        okHttpClient: OkHttpClient,
    ): WakeDeviceWrapper {
        return WakeDeviceWrapperImpl(appContext, dataStore, okHttpClient)
    }
}
