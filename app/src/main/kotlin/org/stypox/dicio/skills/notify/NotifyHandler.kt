package org.stypox.dicio.skills.notify

import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

open class NotifyHandler: NotificationListenerService() {
    companion object Companion {
        var Instance: NotifyHandler? = null
        const val TAG = "Notify"
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind called")
        return null
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
            val appName = statusBarNotification.packageName
            val message = statusBarNotification.notification.extras.getString("android.text")
            notifications.add(Notification(appName, message))
        }
        return notifications
    }
}

data class Notification(val appName: String, val message: String?)