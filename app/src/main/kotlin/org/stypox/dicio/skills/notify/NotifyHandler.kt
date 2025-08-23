package org.stypox.dicio.skills.notify

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi

class NotifyHandler: NotificationListenerService() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun getActiveNotificationsList(): List<Notification> {
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