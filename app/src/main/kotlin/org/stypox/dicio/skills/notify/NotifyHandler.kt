package org.stypox.dicio.skills.notify

import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.stypox.dicio.R

open class NotifyHandler: NotificationListenerService() {
    companion object Companion {
        var Instance: NotifyHandler? = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Instance = this
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onDestroy() {
        super.onDestroy()
        Instance = null
    }

    fun getActiveNotificationsList(): List<Notification> {
        val activeStatusBarNotifications: Array<StatusBarNotification> = getActiveNotifications()

        val notifications = mutableListOf<Notification>()
        for (statusBarNotification in activeStatusBarNotifications) {
            if (statusBarNotification.notification.extras.getString("android.text") == null) {
                continue
            }
            var appName: String
            try {
                val pm: PackageManager = packageManager
                val ai = pm.getApplicationInfo(statusBarNotification.packageName, 0)
                appName = pm.getApplicationLabel(ai).toString()
            }
            catch(e: Exception) {
                e.printStackTrace()
                appName = getString(R.string.appName_error)
            }

            val message = statusBarNotification.notification.extras.getString("android.text")
            notifications.add(Notification(appName, message))
        }
        return notifications
    }
}

data class Notification(val appName: String, val message: String?)