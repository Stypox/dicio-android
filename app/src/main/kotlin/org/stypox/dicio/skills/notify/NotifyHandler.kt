package org.stypox.dicio.skills.notify

import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import org.stypox.dicio.R

open class NotifyHandler: NotificationListenerService() {
    companion object Companion {
        var Instance: NotifyHandler? = null
        const val TAG = "Notify"
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind called")
        return super.onBind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Instance = this
        Log.d(TAG, "onCreate called")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "onListenerConnected called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Instance = null
        Log.d(TAG, "onDestroy called")
    }

    fun getActiveNotificationsList(): List<Notification> {
        Log.d(TAG, "getActiveNotificationsList called")
        val activeStatusBarNotifications: Array<StatusBarNotification> = getActiveNotifications()

        val notifications = mutableListOf<Notification>()
        for (statusBarNotification in activeStatusBarNotifications) {
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