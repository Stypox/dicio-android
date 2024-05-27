package org.stypox.dicio.error

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.stypox.dicio.BuildConfig
import org.stypox.dicio.R
import org.stypox.dicio.ui.theme.AppTheme
import java.util.Locale

@Composable
private fun ErrorScreen(
    userAction: UserAction,
    locale: Locale,
    timestamp: String,
    osInfo: String,
    stackTrace: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.error_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                }
            )
        }
    ) {
        ErrorScreen(
            userAction = userAction,
            locale = locale,
            timestamp = timestamp,
            osInfo = osInfo,
            stackTrace = stackTrace,
            onCopy = onCopy,
            onShare = onShare,
            onReport = onReport,
            modifier = Modifier.padding(it),
        )
    }
}

@Composable
private fun ErrorScreen(
    userAction: UserAction,
    locale: Locale,
    timestamp: String,
    osInfo: String,
    stackTrace: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = stringResource(R.string.error_sorry),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        ButtonsSection(
            onCopy = onCopy,
            onShare = onShare,
            onReport = onReport,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        )

        DetailsSection(
            userAction = userAction,
            locale = locale,
            timestamp = timestamp,
            osInfo = osInfo,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        )

        StackTraceSection(
            stackTrace = stackTrace,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        )
    }
}

@Preview
@Composable
private fun ErrorScreenPreview() {
    AppTheme {
        ErrorScreen(
            userAction = UserAction.STT_SERVICE_SPEECH_TO_TEXT,
            locale = Locale.getDefault(),
            timestamp = "2024-05-27 08:52 ".repeat(10),
            osInfo = "Linux Android 14 - 34",
            stackTrace = ExceptionUtils.getStackTraceString(Exception()),
            onCopy = {},
            onShare = {},
            onReport = {},
            onBack = {},
        )
    }
}

@Composable
private fun DetailsSection(
    userAction: UserAction,
    locale: Locale,
    timestamp: String,
    osInfo: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.error_details_headline),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Text(
                text = stringResource(id = R.string.error_details_labels),
                textAlign = TextAlign.Right,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = StringBuilder(userAction.message)
                    .append('\n').append(locale.toString())
                    .append('\n').append(timestamp)
                    .append('\n').append(BuildConfig.APPLICATION_ID)
                    .append('\n').append(BuildConfig.VERSION_NAME)
                    .append('\n').append(osInfo)
                    .toString(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StackTraceSection(stackTrace: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.error_crash_log_headline),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = stackTrace,
            fontFamily = FontFamily(android.graphics.Typeface.MONOSPACE),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        )
    }
}

@Composable
private fun ButtonsSection(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.error_github_notice),
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FilledTonalButton(
            onClick = onCopy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.error_copy_formatted))
        }

        FilledTonalButton(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.share))
        }

        FilledTonalButton(
            onClick = onReport,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.error_report_github))
        }
    }
}
