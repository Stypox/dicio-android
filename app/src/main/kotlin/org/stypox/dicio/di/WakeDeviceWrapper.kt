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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.stypox.dicio.io.wake.WakeDevice
import org.stypox.dicio.io.wake.WakeState
import org.stypox.dicio.io.wake.oww.OpenWakeWordDevice
import org.stypox.dicio.settings.datastore.UserSettings
import org.stypox.dicio.settings.datastore.WakeDevice.UNRECOGNIZED
import org.stypox.dicio.settings.datastore.WakeDevice.WAKE_DEVICE_NOTHING
import org.stypox.dicio.settings.datastore.WakeDevice.WAKE_DEVICE_OWW
import org.stypox.dicio.settings.datastore.WakeDevice.WAKE_DEVICE_UNSET
import org.stypox.dicio.util.distinctUntilChangedBlockingFirst
import javax.inject.Singleton

interface WakeDeviceWrapper {
    val state: StateFlow<WakeState?>

    val currentDevice: StateFlow<WakeDevice?>

    fun download()

    fun processFrame(audio16bitPcm: ShortArray): Boolean

    fun frameSize(): Int

    fun releaseResources()

    fun isHeyDicio(): Boolean
}

typealias DataStoreWakeDevice = org.stypox.dicio.settings.datastore.WakeDevice

class WakeDeviceWrapperImpl(
    @ApplicationContext private val appContext: Context,
    dataStore: DataStore<UserSettings>,
    private val okHttpClient: OkHttpClient,
) : WakeDeviceWrapper {
    private val scope = CoroutineScope(Dispatchers.Default)

    private var currentSetting: DataStoreWakeDevice
    private var lastFrameHadWrongSize = false

    // null means that the user has not enabled any STT input device
    private val _state: MutableStateFlow<WakeState?> = MutableStateFlow(null)
    override val state: StateFlow<WakeState?> = _state

    private val _currentDevice: MutableStateFlow<WakeDevice?>
    override val currentDevice: StateFlow<WakeDevice?>

    init {
        // Run blocking, because the data store is always available right away since LocaleManager
        // also initializes in a blocking way from the same data store.
        val (firstWakeDevice, nextWakeDeviceFlow) = dataStore.data
            .map { it.wakeDevice }
            .distinctUntilChangedBlockingFirst()

        currentSetting = firstWakeDevice
        _currentDevice = MutableStateFlow(buildInputDevice(firstWakeDevice))
        currentDevice = _currentDevice

        scope.launch {
            _currentDevice.collectLatest { newWakeDevice ->
                if (newWakeDevice == null) {
                    _state.emit(null)
                } else {
                    newWakeDevice.state.collect { _state.emit(it) }
                }
            }
        }

        scope.launch {
            nextWakeDeviceFlow.collect(::changeWakeDeviceTo)
        }
    }

    private fun changeWakeDeviceTo(setting: DataStoreWakeDevice) {
        currentSetting = setting
        val newWakeDevice = buildInputDevice(setting)
        lastFrameHadWrongSize = false
        _currentDevice.update { prevWakeDevice ->
            prevWakeDevice?.destroy()
            newWakeDevice
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

    override fun download() {
        _currentDevice.value?.download()
    }

    override fun processFrame(audio16bitPcm: ShortArray): Boolean {
        val device = _currentDevice.value
            ?: throw IllegalArgumentException("No wake word device is enabled")

        if (audio16bitPcm.size != device.frameSize()) {
            if (lastFrameHadWrongSize) {
                // a single badly-sized frame may happen when switching wake device, so we can
                // tolerate it, but otherwise it is a programming error and should be reported
                throw IllegalArgumentException("Wrong audio frame size: expected ${
                    device.frameSize()} samples but got ${audio16bitPcm.size}")
            }
            lastFrameHadWrongSize = true
            return false

        } else {
            // process the frame only if it has the correct size
            lastFrameHadWrongSize = false
            return device.processFrame(audio16bitPcm)
        }
    }

    override fun frameSize(): Int {
        return _currentDevice.value?.frameSize() ?: 0
    }

    override fun releaseResources() {
        changeWakeDeviceTo(currentSetting)
    }

    override fun isHeyDicio(): Boolean =
        // true by default
        _currentDevice.value?.isHeyDicio() ?: true
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
