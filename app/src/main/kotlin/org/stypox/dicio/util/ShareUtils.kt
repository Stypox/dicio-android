package org.stypox.dicio.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import org.stypox.dicio.MainActivity
import org.stypox.dicio.R
import org.stypox.dicio.io.input.stt_popup.SttPopupActivity
import org.stypox.dicio.util.ShareUtils.openAppChooser
import org.stypox.dicio.util.ShareUtils.openUrlInApp


/**
 * @author Most, if not all, of this class was taken from NewPipe,
 * file `util/external_communication/ShareUtils.java`
 */
object ShareUtils {
    val TAG = ShareUtils::class.simpleName

    /**
     * Open the url with the system default browser. If no browser is installed, falls back to
     * [openAppChooser] (for displaying that no apps are available to handle the action, or possible
     * OEM-related edge cases).
     * <p>
     * This function selects the package to open based on which apps respond to the {@code http://}
     * schema alone, which should exclude special non-browser apps that are can handle the url (e.g.
     * the official YouTube app).
     * <p>
     * Therefore <b>please prefer [openUrlInApp], that handles package
     * resolution in a standard way, unless this is the action of an explicit "Open in browser"
     * button.
     *
     * @param context the context to use
     * @param url     the url to browse
     * @author Taken from NewPipe, file `util/external_communication/ShareUtils.java`
     **/
    fun openUrlInBrowser(context: Context, url: String) {
        // Target a generic http://, so we are sure to get a browser and not e.g. the yt app.
        // Note that this requires the `http` schema to be added to `<queries>` in the manifest.
        val browserIntent = Intent(Intent.ACTION_VIEW, "http://".toUri())

        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // See https://stackoverflow.com/a/58801285 and `setSelector` documentation
        intent.setSelector(browserIntent)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // No browser is available. This should, in the end, yield a nice AOSP error message
            // indicating that no app is available to handle this action.
            //
            // Note: there are some situations where modified OEM ROMs have apps that appear
            // to be browsers but are actually app choosers. If starting the Activity fails
            // related to this, opening the system app chooser is still the correct behavior.
            intent.setSelector(null)
            openAppChooser(context, intent, true)
        }
    }

    /**
     * Open a url with the system default app using [Intent.ACTION_VIEW], showing a toast in
     * case of failure.
     *
     * @param context the context to use
     * @param url     the url to open
     * @author Taken from NewPipe, file `util/external_communication/ShareUtils.java`
     */
    fun openUrlInApp(context: Context, url: String) {
        openIntentInApp(
            context,
            Intent(Intent.ACTION_VIEW, url.toUri()).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /**
     * Open an intent with the system default app.
     *
     *
     * Use [.openIntentInApp] to show a toast in case of failure.
     *
     * @param context the context to use
     * @param intent  the intent to open
     * @return true if the intent could be opened successfully, false otherwise
     * @author Taken from NewPipe, file `util/external_communication/ShareUtils.java`
     */
    fun tryOpenIntentInApp(context: Context, intent: Intent): Boolean {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            return false
        }
        return true
    }

    /**
     * Open an intent with the system default app, showing a toast in case of failure.
     *
     *
     * Use [.tryOpenIntentInApp] if you don't want the toast. Use [ ][.openUrlInApp] as a shorthand for [Intent.ACTION_VIEW] with urls.
     *
     * @param context the context to use
     * @param intent  the intent to
     * @author Taken from NewPipe, file `util/external_communication/ShareUtils.java`
     */
    fun openIntentInApp(context: Context, intent: Intent) {
        if (!tryOpenIntentInApp(context, intent)) {
            Toast.makeText(context, R.string.no_app_to_open_intent, Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Open the system chooser to launch an intent.
     *
     *
     * This method opens an [android.content.Intent.ACTION_CHOOSER] of the intent putted
     * as the intent param. If the setTitleChooser boolean is true, the string "Open with" will be
     * set as the title of the system chooser.
     * For Android P and higher, title for [android.content.Intent.ACTION_SEND] system
     * choosers must be set on this intent, not on the
     * [android.content.Intent.ACTION_CHOOSER] intent.
     *
     * @param context         the context to use
     * @param intent          the intent to open
     * @param setTitleChooser set the title "Open with" to the chooser if true, else not
     * @author Taken from NewPipe, file `util/external_communication/ShareUtils.java`
     */
    private fun openAppChooser(
        context: Context,
        intent: Intent,
        setTitleChooser: Boolean
    ) {
        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, intent)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (setTitleChooser) {
            chooserIntent.putExtra(Intent.EXTRA_TITLE, context.getString(R.string.open_with))
        }


        // Avoid opening in Dicio
        // (Implementation note: if the URL is one for which Dicio itself
        // is set as handler on Android >= 12, we actually remove the only eligible app
        // for this link, and browsers will not be offered to the user. For that, use
        // `openUrlInBrowser`.)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            chooserIntent.putExtra(
                Intent.EXTRA_EXCLUDE_COMPONENTS,
                arrayOf(
                    // the two activities which have exported=true in the manifest
                    ComponentName(context, MainActivity::class.java),
                    ComponentName(context, SttPopupActivity ::class.java),
                )
            )
        }

        // Migrate any clip data and flags from the original intent.
        val permFlags: Int = intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        if (permFlags != 0) {
            var targetClipData = intent.clipData
            if (targetClipData == null && intent.data != null) {
                val item = ClipData.Item(intent.data)
                val mimeTypes = intent.type?.let { arrayOf(it) } ?: arrayOf()
                targetClipData = ClipData(null, mimeTypes, item)
            }
            if (targetClipData != null) {
                chooserIntent.clipData = targetClipData
                chooserIntent.addFlags(permFlags)
            }
        }

        try {
            context.startActivity(chooserIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_app_to_open_intent, Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Open the android share sheet to share a content.
     *
     * For Android 10+ users, a content preview is shown, which includes the title of the shared
     * content. TODO This method does not yet support sharing the image of the content, as Android
     * needs a local Uri to an already loaded image.
     *
     * @param context         the context to use
     * @param title           the title of the content
     * @param content         the content to share
     * @param imagePreviewUrl the image of the subject
     * @author Taken from NewPipe, file `util/external_communication/ShareUtils.java`
     */
    fun shareText(
        context: Context,
        title: String,
        content: String,
        imagePreviewUrl: String = ""
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, content)
        if (!TextUtils.isEmpty(title)) {
            shareIntent.putExtra(Intent.EXTRA_TITLE, title)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        }

        /* TODO: add the image of the content to Android share sheet with setClipData after
            generating a content URI of this image, then use ClipData.newUri(the content resolver,
            null, the content URI) and set the ClipData to the share intent with
            shareIntent.setClipData(generated ClipData).
        if (!imagePreviewUrl.isEmpty()) {
            //shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }*/

        openAppChooser(context, shareIntent, false)
    }

    /**
     * Copy the text to clipboard, and indicate to the user whether the operation was completed
     * successfully using a Toast.
     *
     * @param context the context to use
     * @param text    the text to copy
     * @author Taken from NewPipe, file `util/external_communication/ShareUtils.java`
     */
    fun copyToClipboard(context: Context, text: String) {
        val clipboardManager = ContextCompat.getSystemService(context, ClipboardManager::class.java)
        if (clipboardManager == null) {
            Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_LONG).show()
            return
        }

        try {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text))
            if (Build.VERSION.SDK_INT < 33) {
                // Android 13 has its own "copied to clipboard" dialog
                Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error when trying to copy text to clipboard", e)
            Toast.makeText(context, R.string.failed_to_copy, Toast.LENGTH_SHORT).show()
        }
    }
}
