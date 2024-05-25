package org.stypox.dicio.io.input.stt_service

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.stypox.dicio.R
import org.stypox.dicio.ui.home.SttFab
import org.stypox.dicio.ui.home.SttState
import org.stypox.dicio.ui.theme.AppTheme
import org.stypox.dicio.util.ShareUtils

@Composable
fun SttServiceBottomSheet(
    customHint: String?,
    onDoneClicked: ((List<Pair<String, Float>>) -> Unit)?,
    onDismissRequest: () -> Unit,
) {
    val viewModel: SttServiceViewModel = hiltViewModel()
    val textFieldValue = viewModel.textFieldValue.collectAsState()
    val sttState = if (viewModel.sttInputDevice == null) {
        SttState.NotAvailable
    } else {
        viewModel.sttInputDevice.uiState.collectAsState().value
    }

    @OptIn(ExperimentalMaterial3Api::class)
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        SttServiceBottomSheet(
            customHint = customHint,
            onDoneClicked = if (onDoneClicked == null) {
                null
            } else { ->
                onDoneClicked(viewModel.getUtterancesWithConfidence())
            },
            onDismissRequest = onDismissRequest,
            sttState = sttState,
            onSttClick = viewModel::onSttClick,
            textFieldValue = textFieldValue.value,
            onTextFieldChange = viewModel::setTextFieldValue,
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                )
            ),
        )
    }
}

@Composable
private fun SttServiceBottomSheet(
    customHint: String?,
    onDoneClicked: (() -> Unit)?,
    onDismissRequest: () -> Unit,
    sttState: SttState,
    onSttClick: () -> Unit,
    textFieldValue: String,
    onTextFieldChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        SttServiceTextField(
            value = textFieldValue,
            onValueChange = onTextFieldChange,
            customHint = customHint,
            enabled = sttState != SttState.Listening,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SttServiceButton(
                icon = Icons.Default.ContentCopy,
                contentDescription = R.string.copy_to_clipboard,
                onClick = {
                    ShareUtils.copyToClipboard(context, textFieldValue)
                    onDismissRequest()
                },
            )

            // boxing the SttFab so that the Box can stretch as much as weight(1.0f) allows,
            // but the nested SttFab will always be as small as possible
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1.0f)
                    .padding(vertical = 16.dp),
            ) {
                SttFab(
                    state = sttState,
                    onClick = onSttClick,
                )
            }

            if (onDoneClicked == null) {
                SttServiceButton(
                    icon = Icons.Default.Share,
                    contentDescription = R.string.share,
                    onClick = {
                        ShareUtils.shareText(context, "", textFieldValue)
                        onDismissRequest()
                    },
                )
            } else {
                SttServiceButton(
                    icon = Icons.Default.Done,
                    contentDescription = R.string.done,
                    onClick = {
                        onDoneClicked()
                        onDismissRequest()
                    },
                )
            }
        }
    }
}

@Composable
private fun SttServiceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    customHint: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val fontStyle = MaterialTheme.typography.titleLarge
        .copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Normal)

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = customHint ?: stringResource(R.string.stt_say_something),
                style = fontStyle,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        textStyle = fontStyle,
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun SttServiceButton(
    icon: ImageVector,
    @StringRes contentDescription: Int,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(contentDescription),
            modifier = Modifier.size(32.dp),
        )
    }
}

@Preview
@Composable
private fun SttServiceBottomSheetPreview() {
    var textFieldValue by rememberSaveable { mutableStateOf("") }

    AppTheme {
        @OptIn(ExperimentalMaterial3Api::class)
        BottomSheetScaffold(
            sheetContent = {
                SttServiceBottomSheet(
                    customHint = null,
                    onDoneClicked = {},
                    onDismissRequest = {},
                    sttState = SttState.NotAvailable,
                    onSttClick = {},
                    textFieldValue = textFieldValue,
                    onTextFieldChange = { textFieldValue = it },
                )
            },
            // make the preview always expanded
            scaffoldState = BottomSheetScaffoldState(
                SheetState(
                    skipPartiallyExpanded = true,
                    density = Density(LocalContext.current),
                    initialValue = SheetValue.Expanded,
                ),
                SnackbarHostState(),
            )
        ) {
            Spacer(modifier = Modifier.padding(it))
        }
    }
}
