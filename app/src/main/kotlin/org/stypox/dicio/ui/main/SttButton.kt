package org.stypox.dicio.ui.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.stypox.dicio.R
import org.stypox.dicio.ui.main.SttUiState.Downloaded
import org.stypox.dicio.ui.main.SttUiState.Downloading
import org.stypox.dicio.ui.main.SttUiState.ErrorDownloading
import org.stypox.dicio.ui.main.SttUiState.ErrorLoading
import org.stypox.dicio.ui.main.SttUiState.ErrorUnzipping
import org.stypox.dicio.ui.main.SttUiState.Listening
import org.stypox.dicio.ui.main.SttUiState.Loaded
import org.stypox.dicio.ui.main.SttUiState.Loading
import org.stypox.dicio.ui.main.SttUiState.NoMicrophonePermission
import org.stypox.dicio.ui.main.SttUiState.NotDownloaded
import org.stypox.dicio.ui.main.SttUiState.NotLoaded
import org.stypox.dicio.ui.main.SttUiState.Unzipping
import org.stypox.dicio.ui.util.LoadingProgress
import org.stypox.dicio.ui.util.SmallCircularProgressIndicator
import org.stypox.dicio.ui.util.SttStatesPreviews
import org.stypox.dicio.ui.util.loadingProgressString

/**
 * Calls [SttFabImpl] with the data from the view model, and handles the microhone permission.
 */
@Composable
fun SttFab(state: SttUiState, onClick: () -> Unit) {
    var microphonePermissionGranted by remember { mutableStateOf(true) }
    val context = LocalContext.current
    LaunchedEffect(null) {
        microphonePermissionGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        microphonePermissionGranted = isGranted
    }

    SttFabImpl(
        state = if (microphonePermissionGranted) state else NoMicrophonePermission,
        onClick = if (microphonePermissionGranted)
            onClick
        else
            { -> launcher.launch(Manifest.permission.RECORD_AUDIO) },
    )
}

/**
 * Renders a multi-use [ExtendedFloatingActionButton] that shows the current Stt state, and allows
 * to perform corresponding actions (downloading/unzipping/loading/listening) when pressed.
 */
@Composable
private fun SttFabImpl(state: SttUiState, onClick: () -> Unit) {
    val text = sttFabText(state)
    var lastNonEmptyText by remember { mutableStateOf(text) }
    LaunchedEffect(text) {
        if (text != lastNonEmptyText && text.isNotEmpty()) {
            lastNonEmptyText = text
        }
    }

    ExtendedFloatingActionButton(
        text = { Text(lastNonEmptyText) },
        icon = { SttFabIcon(state, contentDescription = text) },
        onClick = onClick,
        expanded = text.isNotEmpty(),
    )
}

@Composable
private fun sttFabText(state: SttUiState): String {
    return when (state) {
        NoMicrophonePermission -> stringResource(R.string.grant_microphone_permission)
        NotDownloaded -> stringResource(R.string.download_stt)
        is Downloading -> loadingProgressString(
            LocalContext.current,
            state.currentBytes,
            state.totalBytes,
        )
        is ErrorDownloading -> stringResource(R.string.error_downloading)
        Downloaded -> stringResource(R.string.unzip_stt)
        is Unzipping -> stringResource(R.string.unzipping)
        is ErrorUnzipping -> stringResource(R.string.error_unzipping)
        NotLoaded -> ""
        is Loading -> ""
        is ErrorLoading -> stringResource(R.string.error_loading)
        is Loaded -> ""
        is Listening -> stringResource(R.string.listening)
    }
}

@Composable
private fun SttFabIcon(state: SttUiState, contentDescription: String) {
    when (state) {
        NoMicrophonePermission -> Icon(Icons.Default.Warning, contentDescription)
        NotDownloaded -> Icon(Icons.Default.Download, contentDescription)
        is Downloading -> LoadingProgress(state.currentBytes, state.totalBytes)
        is ErrorDownloading -> Icon(Icons.Default.Error, contentDescription)
        Downloaded -> Icon(Icons.Default.FolderZip, contentDescription)
        is Unzipping -> LoadingProgress(state.currentBytes, state.totalBytes)
        is ErrorUnzipping -> Icon(Icons.Default.Error, contentDescription)
        NotLoaded -> Icon(Icons.Default.MicNone, stringResource(R.string.start_listening))
        is Loading -> if (state.thenStartListening)
            SmallCircularProgressIndicator()
        else // show the microphone if the model is loading but is not going to listen
            Icon(Icons.Default.MicNone, stringResource(R.string.start_listening))
        is ErrorLoading -> Icon(Icons.Default.Error, contentDescription)
        is Loaded -> Icon(Icons.Default.MicNone, stringResource(R.string.start_listening))
        is Listening -> Icon(Icons.Default.Mic, contentDescription)
    }
}

@Preview
@Composable
private fun SttFabPreview(@PreviewParameter(SttStatesPreviews::class) state: SttUiState) {
    Column {
        Text(
            text = state.toString(),
            maxLines = 1,
            fontSize = 9.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .background(Color.White.copy(alpha = 0.5f))
                .width(256.dp)
        )
        SttFabImpl(
            state = state,
            onClick = {},
        )
    }
}
