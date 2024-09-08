package org.stypox.dicio.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.stypox.dicio.R
import org.stypox.dicio.error.ErrorInfo
import org.stypox.dicio.error.ErrorUtils
import org.stypox.dicio.error.ExceptionUtils
import org.stypox.dicio.error.UserAction
import org.stypox.dicio.io.wake.WakeState
import org.stypox.dicio.ui.util.LoadingProgress
import org.stypox.dicio.ui.util.WakeStatesPreviews
import org.stypox.dicio.ui.util.loadingProgressString

@Composable
fun WakeWordWidget(
    wakeState: WakeState,
    onWakeDownload: () -> Unit,
    onWakeDisable: () -> Unit,
) {
    MessageCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.wake_word_setup_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (wakeState is WakeState.ErrorDownloading) {
                if (ExceptionUtils.isNetworkError(wakeState.throwable)) {
                    stringResource(R.string.wake_word_error_downloading_network)
                } else {
                    stringResource(R.string.wake_word_error_downloading)
                }
            } else if (wakeState is WakeState.ErrorLoading) {
                stringResource(R.string.wake_word_error_loading)
            } else {
                stringResource(R.string.wake_word_setup_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onWakeDisable) {
                Text(text = stringResource(R.string.disable))
            }

            val error = when (wakeState) {
                is WakeState.ErrorDownloading -> ErrorInfo(wakeState.throwable, UserAction.WAKE_DOWNLOADING)
                is WakeState.ErrorLoading -> ErrorInfo(wakeState.throwable, UserAction.WAKE_LOADING)
                else -> null
            }
            if (error != null) {
                val context = LocalContext.current
                TextButton(onClick = { ErrorUtils.openActivity(context, error) }) {
                    Text(text = stringResource(R.string.error_report))
                }
            }

            when {
                wakeState is WakeState.NotDownloaded ->
                    ElevatedButton(onClick = onWakeDownload) {
                        Text(text = stringResource(R.string.download))
                    }

                error != null ->
                    ElevatedButton(onClick = onWakeDownload) {
                        Text(text = stringResource(R.string.retry))
                    }

                wakeState is WakeState.Downloading ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LoadingProgress(
                            currentBytes = wakeState.currentBytes,
                            totalBytes = wakeState.totalBytes,
                        )

                        Text(
                            text = loadingProgressString(
                                LocalContext.current,
                                wakeState.currentBytes,
                                wakeState.totalBytes,
                            )
                        )
                    }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview
@Composable
private fun WakeWordWidgetPreview(@PreviewParameter(WakeStatesPreviews::class) wakeState: WakeState) {
    WakeWordWidget(
        wakeState = wakeState,
        onWakeDownload = {},
        onWakeDisable = {},
    )
}
