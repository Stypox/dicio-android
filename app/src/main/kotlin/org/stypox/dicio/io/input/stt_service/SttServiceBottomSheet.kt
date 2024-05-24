package org.stypox.dicio.io.input.stt_service

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.stypox.dicio.R
import org.stypox.dicio.ui.home.SttFab
import org.stypox.dicio.ui.home.SttState
import org.stypox.dicio.ui.theme.AppTheme

@Composable
fun SttServiceBottomSheet(
    customHint: String?,
    onDoneClicked: ((List<Pair<String, Float>>) -> Unit)?,
    onDismissRequest: () -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SttServiceTextField(
            input = input,
            onInputChange = { input = it },
            customHint = customHint,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            SttServiceButton(
                icon = Icons.Default.ContentCopy,
                contentDescription = R.string.copy_to_clipboard,
                onClick = { /*TODO*/ },
            )

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f))

            SttFab(
                state = SttState.Listening,
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f))

            if (onDoneClicked == null) {
                SttServiceButton(
                    icon = Icons.Default.Share,
                    contentDescription = R.string.share,
                    onClick = { /*TODO*/ },
                )
            } else {
                SttServiceButton(
                    icon = Icons.Default.Done,
                    contentDescription = R.string.done,
                    onClick = { /*TODO*/ },
                )
            }
        }
    }
}

@Composable
private fun SttServiceTextField(
    input: String,
    onInputChange: (String) -> Unit,
    customHint: String?,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = input,
        onValueChange = onInputChange,
        placeholder = {
            Text(
                text = customHint ?: stringResource(R.string.stt_say_something),
                textAlign = TextAlign.Center,
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
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
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
    AppTheme {
        @OptIn(ExperimentalMaterial3Api::class)
        BottomSheetScaffold(
            sheetContent = {
                SttServiceBottomSheet(
                    customHint = null,
                    onDoneClicked = {},
                    onDismissRequest = {},
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
