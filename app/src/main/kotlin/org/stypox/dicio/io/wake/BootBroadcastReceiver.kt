package org.stypox.dicio.io.wake

import android.Manifest.permission.RECORD_AUDIO
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import org.stypox.dicio.di.WakeDeviceWrapper
import javax.inject.Inject

@AndroidEntryPoint
class BootBroadcastReceiver : BroadcastReceiver() {
    @Inject lateinit var wakeDevice: WakeDeviceWrapper

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        if (ContextCompat.checkSelfPermission(context, RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED) {
            return
        }

        when (wakeDevice.state.value) {
            WakeState.NotLoaded,
            WakeState.Loading,
            WakeState.Loaded -> {
                // any of these three states indicates that wake word recognition is enabled, and
                // that the model has already been downloaded

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Starting from Android 14, it is not possible to start a foreground service
                    // that accesses the microphone from a BOOT_COMPLETED broadcast. So we show a
                    // notification instead, which starts the foreground service when clicked.
                    // https://developer.android.com/about/versions/15/behavior-changes-15#fgs-boot-completed
                    WakeService.createNotificationToStartLater(context)
                } else {
                    WakeService.start(context)
                }
            }
            else -> {}
        }
    }
}
