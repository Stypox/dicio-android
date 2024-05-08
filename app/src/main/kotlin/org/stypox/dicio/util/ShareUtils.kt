package org.stypox.dicio.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.stypox.dicio.R

/**
 * @implNote Most, if not all, of this class was taken from NewPipe, file util/ShareUtils.java
 */
object ShareUtils {
    /**
     * Open the url with the system default browser. If no browser is set as default, fallbacks to
     * [openAppChooser].
     *
     * @param context                the context to use
     * @param url                    the url to browse
     * @param httpDefaultBrowserTest the boolean to set if the test for the default browser will be
     * for HTTP protocol or for the created intent
     * @return true if the URL can be opened or false if it cannot
     * @implNote Taken from NewPipe, file util/ShareUtils.java
     */
    @JvmOverloads
    fun openUrlInBrowser(
        context: Context,
        url: String,
        httpDefaultBrowserTest: Boolean = true
    ): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val defaultPackageName = if (httpDefaultBrowserTest) {
            getDefaultAppPackageName(
                context, Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://")
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            getDefaultAppPackageName(context, intent)
        }

        if (defaultPackageName == "android") {
            // No browser set as default (doesn't work on some devices)
            openAppChooser(context, intent, true)
        } else if (defaultPackageName.isEmpty()) {
            // No app installed to open a web url
            Toast.makeText(context, R.string.no_app_to_open_intent, Toast.LENGTH_LONG).show()
            return false
        } else {
            try {
                intent.setPackage(defaultPackageName)
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Not a browser but an app chooser because of OEMs changes
                intent.setPackage(null)
                openAppChooser(context, intent, true)
            }
        }
        return true
    }

    /**
     * Open an intent with the system default app.
     *
     *
     * The intent can be of every type, excepted a web intent for which
     * [openUrlInBrowser] should be used.
     *
     *
     * If no app can open the intent, a toast with the message `No app on your device can
     * open this` is shown.
     *
     * @param context   the context to use
     * @param intent    the intent to open
     * @param showToast a boolean to set if a toast is displayed to user when no app is installed
     * to open the intent (true) or not (false)
     * @return true if the intent can be opened or false if it cannot be
     * @implNote Taken from NewPipe, file util/ShareUtils.java
     */
    fun openIntentInApp(
        context: Context,
        intent: Intent,
        showToast: Boolean
    ): Boolean {
        val defaultPackageName = getDefaultAppPackageName(context, intent)
        if (defaultPackageName.isEmpty()) {
            // No app installed to open the intent
            if (showToast) {
                Toast.makeText(context, R.string.no_app_to_open_intent, Toast.LENGTH_LONG)
                    .show()
            }
            return false
        } else {
            context.startActivity(intent)
        }
        return true
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
     * @implNote Taken from NewPipe, file util/ShareUtils.java
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
        context.startActivity(chooserIntent)
    }

    /**
     * Get the default app package name.
     *
     *
     * If no app is set as default, it will return "android" (not on some devices because some
     * OEMs changed the app chooser).
     *
     *
     * If no app is installed on user's device to handle the intent, it will return an empty string.
     *
     * @param context the context to use
     * @param intent  the intent to get default app
     * @return the package name of the default app, an empty string if there's no app installed to
     * handle the intent or the app chooser if there's no default
     * @implNote Taken from NewPipe, file util/ShareUtils.java
     */
    private fun getDefaultAppPackageName(
        context: Context,
        intent: Intent
    ): String {
        val resolveInfo = context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return if (resolveInfo == null) {
            ""
        } else {
            resolveInfo.activityInfo.packageName
        }
    }
    /**
     * Open the android share sheet to share a content.
     *
     * For Android 10+ users, a content preview is shown, which includes the title of the shared
     * content.
     * Support sharing the image of the content needs to done, if possible.
     *
     * @param context         the context to use
     * @param title           the title of the content
     * @param content         the content to share
     * @param imagePreviewUrl the image of the subject
     * @implNote Taken from NewPipe, file util/ShareUtils.java
     */
    @JvmOverloads
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
        }*/openAppChooser(context, shareIntent, false)
    }

    /**
     * Copy the text to clipboard, and indicate to the user whether the operation was completed
     * successfully using a Toast.
     *
     * @param context the context to use
     * @param text    the text to copy
     * @implNote Taken from NewPipe, file util/ShareUtils.java
     */
    fun copyToClipboard(context: Context, text: String) {
        val clipboardManager = ContextCompat.getSystemService(context, ClipboardManager::class.java)
        if (clipboardManager == null) {
            Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_LONG).show()
            return
        }
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text))
        Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }
}