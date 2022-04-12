package org.dicio.dicio_android.error;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.dicio.dicio_android.BuildConfig;
import org.dicio.dicio_android.R;
import org.dicio.dicio_android.databinding.ActivityErrorBinding;
import org.dicio.dicio_android.util.BaseActivity;
import org.dicio.dicio_android.util.ShareUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import kotlin.text.Charsets;

/**
 * This activity is used to show error details and allow reporting them in various ways. Use {@link
 * ErrorUtils#openActivity(Context, ErrorInfo)} to correctly open this activity.
 * @implNote Taken with some modifications from NewPipe, file error/ErrorActivity.java
 */
public class ErrorActivity extends BaseActivity {
    // LOG TAGS
    public static final String TAG = ErrorActivity.class.toString();
    // BUNDLE TAGS
    public static final String ERROR_INFO = "error_info";

    public static final String ERROR_GITHUB_ISSUE_URL
            = "https://github.com/Stypox/dicio-android/issues";

    public static final DateTimeFormatter CURRENT_TIMESTAMP_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    private ErrorInfo errorInfo;
    private String currentTimeStamp;

    private ActivityErrorBinding activityErrorBinding;


    ////////////////////////////////////////////////////////////////////////
    // Activity lifecycle
    ////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityErrorBinding = ActivityErrorBinding.inflate(getLayoutInflater());
        setContentView(activityErrorBinding.getRoot());

        setSupportActionBar(activityErrorBinding.toolbar);
        Optional.ofNullable(getSupportActionBar())
                .ifPresent(actionBar -> actionBar.setTitle(R.string.error_title));

        final Intent intent = getIntent();
        errorInfo = intent.getParcelableExtra(ERROR_INFO);
        currentTimeStamp = CURRENT_TIMESTAMP_FORMATTER.format(LocalDateTime.now());

        activityErrorBinding.errorReportCopyButton.setOnClickListener(v ->
                ShareUtils.copyToClipboard(this, buildMarkdown()));
        activityErrorBinding.errorReportGitHubButton.setOnClickListener(v ->
                ShareUtils.openUrlInBrowser(this, ERROR_GITHUB_ISSUE_URL, false));

        // normal bugreport
        buildInfo(errorInfo);
        activityErrorBinding.errorView.setText(errorInfo.getStackTrace());

        // print stack trace once again for debugging:
        Log.e(TAG, errorInfo.getStackTrace());
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.error, menu);
        menu.findItem(R.id.menu_item_share_error).setOnMenuItemClickListener(menuItem -> {
            ShareUtils.shareText(getApplicationContext(),
                    getString(R.string.error_title), buildJson());
            return true;
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private void buildInfo(final ErrorInfo info) {
        activityErrorBinding.errorDetailsView.setText(new StringBuilder()
                .append(getUserActionString(info.getUserAction()))
                .append('\n').append(getAppLocale())
                .append('\n').append(currentTimeStamp)
                .append('\n').append(getPackageName())
                .append('\n').append(BuildConfig.VERSION_NAME)
                .append('\n').append(getOsString()));
    }

    private String buildJson() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (outputStream;
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charsets.UTF_8);
            JsonWriter jsonWriter = new JsonWriter(writer)) {

            jsonWriter
                    .beginObject()
                    .name("user_action").value(getUserActionString(errorInfo.getUserAction()))
                    .name("app_language").value(getAppLocale())
                    .name("package").value(getPackageName())
                    .name("version").value(BuildConfig.VERSION_NAME)
                    .name("os").value(getOsString())
                    .name("time").value(currentTimeStamp)
                    .name("exceptions").beginArray().value(errorInfo.getStackTrace()).endArray()
                    .endObject()
                    .close();

        } catch (final Throwable e) {
            Log.e(TAG, "Could not build json", e);
        }

        return outputStream.toString();
    }

    private String buildMarkdown() {
        try {
            final StringBuilder htmlErrorReport = new StringBuilder();

            // basic error info
            htmlErrorReport
                    .append("## Exception")
                    .append("\n* __User action:__ ")
                        .append(getUserActionString(errorInfo.getUserAction()))
                    .append("\n* __App locale:__ ").append(getAppLocale())
                    .append("\n* __Version:__ ").append(BuildConfig.VERSION_NAME)
                    .append("\n* __OS:__ ").append(getOsString()).append("\n");

            // Collapse the log to a single paragraph when there are more than one
            // to keep the GitHub issue clean.
            if (!errorInfo.getStackTrace().isEmpty()) {
                htmlErrorReport
                        .append("<details><summary><b>Crash log</b></summary><p>\n")
                        .append("\n```\n")
                        .append(errorInfo.getStackTrace())
                        .append("\n```\n")
                        .append("</details>\n");
            }

            return htmlErrorReport.toString();
        } catch (final Throwable e) {
            Log.e(TAG, "Could not build markdown", e);
            return "";
        }
    }

    private String getUserActionString(final UserAction userAction) {
        if (userAction == null) {
            return "Your description is in another castle.";
        } else {
            return userAction.getMessage();
        }
    }

    private String getAppLocale() {
        return Locale.getDefault().toString();
    }

    private String getOsString() {
        final String osBase = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? Build.VERSION.BASE_OS : "Android";
        return System.getProperty("os.name")
                + " " + (osBase.isEmpty() ? "Android" : osBase)
                + " " + Build.VERSION.RELEASE
                + " - " + Build.VERSION.SDK_INT;
    }
}
