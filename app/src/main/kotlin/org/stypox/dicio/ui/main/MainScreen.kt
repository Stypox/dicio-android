package org.stypox.dicio.ui.main

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.dicio.skill.context.SkillContext
import org.stypox.dicio.R
import org.stypox.dicio.di.SkillContextImpl
import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.ui.nav.SearchTopAppBar
import org.stypox.dicio.ui.theme.AppTheme
import org.stypox.dicio.ui.util.InteractionLogPreviews
import org.stypox.dicio.ui.util.SttStatesPreviews
import kotlin.math.abs

@Composable
fun MainScreen(navigationIcon: @Composable () -> Unit) {
    val channel = remember { Channel<Boolean>() }
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        coroutineScope.launch {
            channel.send(isGranted.values.all { it })
        }
    }
    val context = LocalContext.current

    suspend fun requestPermissions(permissions: Array<String>): Boolean {
        if (
            permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            // permissions already granted
            return true
        }

        // some permission is not already granted, need to request it
        launcher.launch(permissions)
        return channel.receive()
    }

    val viewModel: MainScreenViewModel = hiltViewModel()
    // keep assigning permissionRequester at every recomposition because `launcher` changes when
    // the activity is recreated (no rememberSaveable is available)
    viewModel.skillEvaluator.permissionRequester = ::requestPermissions
    val interactionsState by viewModel.skillEvaluator.state.collectAsState()
    val sttState = viewModel.sttInputDevice?.uiState?.collectAsState()

    MainScreen(
        skillContext = viewModel.skillContext,
        interactionLog = interactionsState,
        sttState = sttState?.value,
        onSttClick = viewModel.sttInputDevice?.let { it::onClick } ?: {},
        onManualUserInput = {
            viewModel.inputEventsModule.tryEmitEvent(InputEvent.Final(listOf(it)))
        },
        navigationIcon = navigationIcon,
    )
}

@Composable
fun MainScreen(
    skillContext: SkillContext,
    interactionLog: InteractionLog,
    sttState: SttState?,
    onSttClick: () -> Unit,
    onManualUserInput: (String) -> Unit,
    navigationIcon: @Composable () -> Unit,
) {
    var searchString by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            SearchTopAppBar(
                searchString = searchString,
                setSearchString = { searchString = it },
                onSearch = onManualUserInput,
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
        content = { paddingValues ->
            InteractionList(
                skillContext = skillContext,
                interactionLog = interactionLog,
                onConfirmedQuestionClick = { searchString = it },
                modifier = Modifier.padding(paddingValues),
            )
        },
        floatingActionButton = {
            if (sttState != null) {
                SttFab(
                    state = sttState,
                    onClick = onSttClick,
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    )
}

@Preview
@Composable
private fun MainScreenPreview(@PreviewParameter(InteractionLogPreviews::class) interactionLog: InteractionLog) {
    val sttStatesPreviews = remember { SttStatesPreviews().values.toList() }
    var i by remember { mutableIntStateOf(abs(interactionLog.hashCode())) }

    AppTheme(dynamicColor = false) {
        MainScreen(
            skillContext = SkillContextImpl.newForPreviews(),
            interactionLog = interactionLog,
            sttState = sttStatesPreviews[i % sttStatesPreviews.size],
            onSttClick = { i += 1 },
            onManualUserInput = {},
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
