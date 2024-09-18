package org.stypox.dicio.ui.home

import android.Manifest
import android.os.Build
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.permissionflow.compose.rememberMultiplePermissionState
import dev.shreyaspatil.permissionflow.compose.rememberPermissionFlowRequestLauncher
import org.stypox.dicio.R
import org.stypox.dicio.error.ErrorInfo
import org.stypox.dicio.error.ErrorUtils
import org.stypox.dicio.error.ExceptionUtils
import org.stypox.dicio.error.UserAction
import org.stypox.dicio.io.wake.WakeState
import org.stypox.dicio.io.wake.WakeState.NoMicOrNotificationPermission
import org.stypox.dicio.ui.util.LoadingProgress
import org.stypox.dicio.ui.util.WakeStatesPreviews
import org.stypox.dicio.ui.util.loadingProgressString

val wakeWordPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
else
    arrayOf(Manifest.permission.RECORD_AUDIO)

/**
 * Calls [WakeWordWidgetImpl] with the data from the view model, and handles the permissions.
 * Will not show anything if there's no setup needed.
 */
@Composable
fun WakeWordWidget(
    wakeState: WakeState,
    onWakeDownload: () -> Unit,
    onWakeDisable: () -> Unit,
) {
    val permissionsState by rememberMultiplePermissionState(*wakeWordPermissions)
    val launcher = rememberPermissionFlowRequestLauncher()

    if (!permissionsState.allGranted) {
        WakeWordWidgetImpl(
            wakeState = NoMicOrNotificationPermission,
            onWakeGrantPermissions = { launcher.launch(wakeWordPermissions) },
            onWakeDownload = onWakeDownload,
            onWakeDisable = onWakeDisable,
        )

    } else when (wakeState) {
        WakeState.NotDownloaded,
        is WakeState.Downloading,
        is WakeState.ErrorDownloading,
        is WakeState.ErrorLoading -> WakeWordWidgetImpl(
            wakeState = wakeState,
            onWakeGrantPermissions = {},
            onWakeDownload = onWakeDownload,
            onWakeDisable = onWakeDisable,
        )

        else -> {}
    }
}

@Composable
fun WakeWordWidgetImpl(
    wakeState: WakeState,
    onWakeGrantPermissions: () -> Unit,
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
                wakeState == NoMicOrNotificationPermission ->
                    ElevatedButton(onClick = onWakeGrantPermissions) {
                        Text(text = stringResource(R.string.grant_permissions))
                    }

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
                        Text(loadingProgressString(LocalContext.current, wakeState.progress))
                        LoadingProgress(wakeState.progress)
                    }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview
@Composable
private fun WakeWordWidgetPreview(@PreviewParameter(WakeStatesPreviews::class) wakeState: WakeState) {
    WakeWordWidgetImpl(
        wakeState = wakeState,
        onWakeGrantPermissions = {},
        onWakeDownload = {},
        onWakeDisable = {},
    )
}
