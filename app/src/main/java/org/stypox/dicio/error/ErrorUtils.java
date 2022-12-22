package org.stypox.dicio.error;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.stypox.dicio.R;

/**
 * This class contains all of the methods that should be used to let the user know that an error has
 * occurred in the least intrusive way possible for each case. This class is for unexpected errors,
 * for handled errors (e.g. network errors) use e.g. [ErrorPanelHelper] instead.
 * - Use a snackbar if the exception is not critical and it happens in a place where a root view
 * is available.
 * - Use a notification if the exception happens inside a background service (player, subscription
 * import, ...) or there is no activity/fragment from which to extract a root view.
 * - Finally use the error activity only as a last resort in case the exception is critical and
 * happens in an open activity (since the workflow would be interrupted anyway in that case).
 * @implNote Taken with some modifications from NewPipe, file error/ErrorUtil.kt
 */
public final class ErrorUtils {
    private static final int ERROR_REPORT_NOTIFICATION_ID = 5340681;

    private ErrorUtils() {
    }

    /**
     * Starts a new error activity allowing the user to report the provided error. Only use this
     * method directly as a last resort in case the exception is critical and happens in an open
     * activity (since the workflow would be interrupted anyway in that case). So never use this
     * for background services.
     *
     * @param context   the context to use to start the new activity
     * @param errorInfo the error info to be reported
     */
    public static void openActivity(final Context context, final ErrorInfo errorInfo) {
        context.startActivity(getErrorActivityIntent(context, errorInfo));
    }

    /**
     * Show a bottom snackbar to the user, with a report button that opens the error activity.
     * Use this method if the exception is not critical and it happens in a place where a root
     * view is available.
     *
     * @param context   will be used to obtain the root view if it is an [Activity]; if no root
     *                  view can be found an error notification is shown instead
     * @param errorInfo the error info to be reported
     */
    public static void showSnackbar(final Context context, final ErrorInfo errorInfo) {
        final View rootView = (context instanceof Activity)
                ? ((Activity) context).findViewById(android.R.id.content) : null;
        showSnackbar(context, rootView, errorInfo);
    }

    /**
     * Show a bottom snackbar to the user, with a report button that opens the error activity.
     * Use this method if the exception is not critical and it happens in a place where a root
     * view is available.
     *
     * @param fragment  will be used to obtain the root view if it has a connected [Activity]; if
     *                  no root view can be found an error notification is shown instead
     * @param errorInfo the error info to be reported
     */
    public static void showSnackbar(final Fragment fragment, final ErrorInfo errorInfo) {
        View rootView = fragment.getView();
        if (rootView == null && fragment.getActivity() != null) {
            rootView = fragment.requireActivity().findViewById(R.id.content);
        }
        showSnackbar(fragment.requireContext(), rootView, errorInfo);
    }

    /**
     * Create an error notification. Tapping on the notification opens the error activity. Use
     * this method if the exception happens inside a background service (player, subscription
     * import, ...) or there is no activity/fragment from which to extract a root view.
     *
     * @param context   the context to use to show the notification
     * @param errorInfo the error info to be reported; the error message
     *                  [ErrorInfo.messageStringId] will be shown in the notification
     *                  description
     */
    public static void createNotification(final Context context, final ErrorInfo errorInfo) {
        var pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags = pendingIntentFlags | PendingIntent.FLAG_IMMUTABLE;
        }

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context, context.getString(R.string.error_report_channel_id))
                .setSmallIcon(R.drawable.ic_bug_report_white)
                .setContentTitle(context.getString(R.string.error_report_notification_title))
                .setContentText(errorInfo.getUserAction().getMessage())
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        getErrorActivityIntent(context, errorInfo), pendingIntentFlags));

        NotificationManagerCompat.from(context)
                .notify(ERROR_REPORT_NOTIFICATION_ID, notificationBuilder.build());

        // since the notification is silent, also show a toast, otherwise the user is confused
        Toast.makeText(context, R.string.error_report_notification_toast, Toast.LENGTH_SHORT)
                .show();
    }

    private static Intent getErrorActivityIntent(final Context context, final ErrorInfo errorInfo) {
        final Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(ErrorActivity.ERROR_INFO, errorInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private static void showSnackbar(final Context context,
                                     @Nullable final View rootView,
                                     final ErrorInfo errorInfo) {
        if (rootView == null) {
            // fallback to showing a notification if no root view is available
            createNotification(context, errorInfo);
        } else {
            Snackbar.make(rootView, R.string.error_sorry, Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.error_report).toUpperCase(),
                            (v) -> openActivity(context, errorInfo))
                    .show();
        }
    }
}
