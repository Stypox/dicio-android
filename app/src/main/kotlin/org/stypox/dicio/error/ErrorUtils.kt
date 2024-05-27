package org.stypox.dicio.error

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import org.stypox.dicio.R
import java.util.Locale

/**
 * This class contains all of the methods that should be used to let the user know that an error has
 * occurred in the least intrusive way possible for each case. This class is for unexpected errors,
 * for handled errors (e.g. network errors) use something else instead.
 * - Use a snackbar if the exception is not critical and it happens in a place where a root view
 * is available.
 * - Use a notification if the exception happens inside a background service (player, subscription
 * import, ...) or there is no activity/fragment from which to extract a root view.
 * - Finally use the error activity only as a last resort in case the exception is critical and
 * happens in an open activity (since the workflow would be interrupted anyway in that case).
 * @implNote Taken with some modifications from NewPipe, file error/ErrorUtil.kt
 */
object ErrorUtils {
    private const val ERROR_REPORT_NOTIFICATION_ID = 5340681

    /**
     * Starts a new error activity allowing the user to report the provided error. Only use this
     * method directly as a last resort in case the exception is critical and happens in an open
     * activity (since the workflow would be interrupted anyway in that case). So never use this
     * for background services.
     *
     * @param context   the context to use to start the new activity
     * @param errorInfo the error info to be reported
     */
    fun openActivity(context: Context, errorInfo: ErrorInfo) {
        context.startActivity(getErrorActivityIntent(context, errorInfo))
    }

    /**
     * Create an error notification. Tapping on the notification opens the error activity. Use
     * this method if the exception happens inside a background service (player, subscription
     * import, ...) or there is no activity/fragment from which to extract a root view.
     *
     * @param context   the context to use to show the notification
     * @param errorInfo the error info to be reported; the error message
     * [ErrorInfo.userAction] will be shown in the notification
     * description
     */
    fun createNotification(context: Context, errorInfo: ErrorInfo) {
        var pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
        }
        val notificationBuilder = NotificationCompat.Builder(
            context, context.getString(R.string.error_report_channel_id)
        )
            .setSmallIcon(R.drawable.ic_bug_report_white)
            .setContentTitle(context.getString(R.string.error_report_notification_title))
            .setContentText(errorInfo.userAction.message)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context, 0,
                    getErrorActivityIntent(context, errorInfo), pendingIntentFlags
                )
            )
        NotificationManagerCompat.from(context)
            .notify(ERROR_REPORT_NOTIFICATION_ID, notificationBuilder.build())

        // since the notification is silent, also show a toast, otherwise the user is confused
        Toast.makeText(context, R.string.error_report_notification_toast, Toast.LENGTH_SHORT)
            .show()
    }

    private fun getErrorActivityIntent(context: Context?, errorInfo: ErrorInfo): Intent {
        val intent = Intent(context, ErrorActivity::class.java)
        intent.putExtra(ErrorActivity.ERROR_INFO, errorInfo)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}
