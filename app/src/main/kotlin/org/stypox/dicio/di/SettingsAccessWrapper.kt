package org.stypox.dicio.di

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dicio.skill.context.SettingsAccess
import org.stypox.dicio.settings.datastore.UserSettings
import org.stypox.dicio.settings.datastore.WakeDevice
import org.stypox.dicio.settings.datastore.copy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsAccessWrapper @Inject constructor(
    private val dataStore: DataStore<UserSettings>,
) : SettingsAccess {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var lastObservedSettings: UserSettings = UserSettings.getDefaultInstance()

    init {
        scope.launch {
            dataStore.data.collect {
                lastObservedSettings = it
            }
        }
    }

    override var wakeDeviceEnabled: Boolean
        get() = lastObservedSettings.wakeDevice != WakeDevice.WAKE_DEVICE_NOTHING
        set(value) {
            scope.launch {
                dataStore.updateData {
                    it.copy {
                        wakeDevice = if (value) {
                            WakeDevice.WAKE_DEVICE_OWW
                        } else {
                            WakeDevice.WAKE_DEVICE_NOTHING
                        }
                    }
                }
            }
        }
}
