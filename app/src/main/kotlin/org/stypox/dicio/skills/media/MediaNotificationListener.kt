package org.stypox.dicio.skills.media

import android.service.notification.NotificationListenerService

/**
 * Used to obtain a [android.media.session.MediaSessionManager] to control the media sessions, we
 * don't really need to read data from any notification.
 */
class MediaNotificationListener : NotificationListenerService()