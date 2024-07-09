package org.stypox.dicio.screenshot

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import org.stypox.dicio.di.LocaleManager
import org.stypox.dicio.di.SttInputDeviceWrapper
import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.io.input.SttInputDevice
import org.stypox.dicio.settings.datastore.InputDevice
import org.stypox.dicio.settings.datastore.UserSettings
import org.stypox.dicio.ui.home.SttState

class FakeSttInputDeviceWrapper(
    @ApplicationContext appContext: Context,
    dataStore: DataStore<UserSettings>,
    localeManager: LocaleManager,
    okHttpClient: OkHttpClient
) : SttInputDeviceWrapper(appContext, dataStore, localeManager, okHttpClient) {
    val fakeUiState: MutableStateFlow<SttState> = MutableStateFlow(SttState.NotInitialized)

    override fun buildInputDevice(setting: InputDevice): SttInputDevice {
        return object : SttInputDevice {
            override val uiState: StateFlow<SttState> get() = fakeUiState

            override fun tryLoad(thenStartListeningEventListener: ((InputEvent) -> Unit)?) {
            }

            override fun onClick(eventListener: (InputEvent) -> Unit) {
            }

            override suspend fun destroy() {
            }
        }
    }
}
