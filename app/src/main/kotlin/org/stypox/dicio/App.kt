package org.stypox.dicio

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.HiltAndroidApp
import org.stypox.dicio.util.PermissionUtils

// IMPORTANT NOTE: beware of this nasty bug related to allowBackup=true
// https://medium.com/p/924c91bafcac
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            PermissionUtils.checkPermissions(this, Manifest.permission.POST_NOTIFICATIONS)
        ) {
            initNotificationChannels()
        }
    }

    fun initNotificationChannels() {
        NotificationManagerCompat.from(this).createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat.Builder(
                    getString(R.string.error_report_channel_id),
                    NotificationManagerCompat.IMPORTANCE_LOW
                )
                    .setName(getString(R.string.error_report_channel_name))
                    .setDescription(getString(R.string.error_report_channel_description))
                    .build()
            )
        )
    }
}
