package org.stypox.dicio.error

import android.os.Build
import android.os.Bundle
import android.util.JsonWriter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import org.stypox.dicio.BuildConfig
import org.stypox.dicio.R
import org.stypox.dicio.databinding.ActivityErrorBinding
import org.stypox.dicio.util.BaseActivity
import org.stypox.dicio.util.ShareUtils
import org.stypox.dicio.util.ShareUtils.shareText
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.text.Charsets.UTF_8

/**
 * This activity is used to show error details and allow reporting them in various ways. Use [ErrorUtils.openActivity] to correctly open this activity.
 * @implNote Taken with some modifications from NewPipe, file error/ErrorActivity.java
 */
class ErrorActivity : BaseActivity() {
    private lateinit var errorInfo: ErrorInfo
    private var currentTimeStamp: String? = null
    private lateinit var activityErrorBinding: ActivityErrorBinding

    ////////////////////////////////////////////////////////////////////////
    // Activity lifecycle
    ////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityErrorBinding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(activityErrorBinding.root)
        setSupportActionBar(activityErrorBinding.toolbar)
        supportActionBar?.setTitle(R.string.error_title)

        errorInfo = intent.getParcelableExtra(ERROR_INFO)!!
        currentTimeStamp = CURRENT_TIMESTAMP_FORMATTER.format(LocalDateTime.now())
        activityErrorBinding.errorReportCopyButton.setOnClickListener {
            ShareUtils.copyToClipboard(
                this,
                buildMarkdown()
            )
        }
        activityErrorBinding.errorReportGitHubButton.setOnClickListener {
            ShareUtils.openUrlInBrowser(
                this,
                ERROR_GITHUB_ISSUE_URL,
                false
            )
        }

        // normal bugreport
        buildInfo(errorInfo)
        activityErrorBinding.errorView.text = errorInfo.stackTrace

        // print stack trace once again for debugging:
        Log.e(TAG, errorInfo.stackTrace)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.error, menu)
        menu.findItem(R.id.menu_item_share_error)
            .setOnMenuItemClickListener {
                shareText(
                    applicationContext,
                    getString(R.string.error_title), buildJson()
                )
                true
            }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    private fun buildInfo(info: ErrorInfo) {
        activityErrorBinding.errorDetailsView.text = StringBuilder()
            .append(getUserActionString(info.userAction))
            .append('\n').append(appLocale)
            .append('\n').append(currentTimeStamp)
            .append('\n').append(packageName)
            .append('\n').append(BuildConfig.VERSION_NAME)
            .append('\n').append(osString)
    }

    private fun buildJson(): String {
        val outputStream = ByteArrayOutputStream()
        try {
            outputStream.use {
                OutputStreamWriter(outputStream, UTF_8).use { writer ->
                    JsonWriter(writer).use { jsonWriter ->
                        jsonWriter
                            .beginObject()
                            .name("user_action")
                            .value(getUserActionString(errorInfo.userAction))
                            .name("app_language").value(appLocale)
                            .name("package").value(packageName)
                            .name("version").value(BuildConfig.VERSION_NAME)
                            .name("os").value(osString)
                            .name("time").value(currentTimeStamp)
                            .name("exceptions").beginArray().value(errorInfo.stackTrace)
                            .endArray()
                            .endObject()
                            .close()
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Could not build json", e)
        }
        return outputStream.toString()
    }

    private fun buildMarkdown(): String {
        return try {
            val htmlErrorReport = StringBuilder()

            // basic error info
            htmlErrorReport
                .append("## Exception")
                .append("\n* __User action:__ ")
                .append(getUserActionString(errorInfo.userAction))
                .append("\n* __App locale:__ ").append(appLocale)
                .append("\n* __Version:__ ").append(BuildConfig.VERSION_NAME)
                .append("\n* __OS:__ ").append(osString).append("\n")

            // Collapse the log to a single paragraph when there are more than one
            // to keep the GitHub issue clean.
            if (errorInfo.stackTrace.isNotEmpty()) {
                htmlErrorReport
                    .append("<details><summary><b>Crash log</b></summary><p>\n")
                    .append("\n```\n")
                    .append(errorInfo.stackTrace)
                    .append("\n```\n")
                    .append("</details>\n")
            }
            htmlErrorReport.toString()
        } catch (e: Throwable) {
            Log.e(TAG, "Could not build markdown", e)
            ""
        }
    }

    private fun getUserActionString(userAction: UserAction?): String {
        return userAction?.message ?: "Your description is in another castle."
    }

    private val appLocale: String
        get() = Locale.getDefault().toString()
    private val osString: String
        get() {
            val osBase = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                Build.VERSION.BASE_OS
            else
                "Android"

            return ((System.getProperty("os.name") ?: "")
                    + " " + osBase.ifEmpty { "Android" }
                    + " " + Build.VERSION.RELEASE
                    + " - " + Build.VERSION.SDK_INT)
        }

    companion object {
        // LOG TAGS
        val TAG = ErrorActivity::class.java.toString()

        // BUNDLE TAGS
        const val ERROR_INFO = "error_info"
        const val ERROR_GITHUB_ISSUE_URL = "https://github.com/Stypox/dicio-android/issues"
        val CURRENT_TIMESTAMP_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
