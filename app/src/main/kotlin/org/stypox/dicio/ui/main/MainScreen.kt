package org.stypox.dicio.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import org.stypox.dicio.R
import org.stypox.dicio.ui.nav.SearchTopAppBar
import org.stypox.dicio.ui.theme.AppTheme
import org.stypox.dicio.ui.util.InteractionLogPreviews

@Composable
fun MainScreen(
    interactionLog: InteractionLog,
    navigationIcon: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            SearchTopAppBar(
                onSearch = {},
                hint = stringResource(R.string.text_input_hint),
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                searchIcon = {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                    )
                },
                navigationIcon = navigationIcon,
            )
        },
        content = {
            ConversationList(
                interactionLog = interactionLog,
                modifier = Modifier.padding(it),
            )
        }
    )
}

@Preview
@Composable
private fun MainScreenPreview(@PreviewParameter(InteractionLogPreviews::class) interactionLog: InteractionLog) {
    AppTheme(dynamicColor = false) {
        MainScreen(
            interactionLog = interactionLog,
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                    )
                }
            }
        )
    }
}
