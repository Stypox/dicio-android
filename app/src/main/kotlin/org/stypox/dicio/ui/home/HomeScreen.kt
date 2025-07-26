package org.stypox.dicio.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.hilt.navigation.compose.hiltViewModel
import dev.shreyaspatil.permissionflow.compose.rememberPermissionFlowRequestLauncher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Permission
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.di.SkillContextImpl
import org.stypox.dicio.eval.SkillEvaluator
import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.io.input.SttState
import org.stypox.dicio.io.wake.WakeState
import org.stypox.dicio.ui.nav.SearchTopAppBar
import org.stypox.dicio.ui.theme.AppTheme
import org.stypox.dicio.ui.util.InteractionLogPreviews
import org.stypox.dicio.ui.util.SkillInfoPreviews
import org.stypox.dicio.ui.util.SttStatesPreviews
import org.stypox.dicio.util.checkPermissions
import org.stypox.dicio.util.getNonGrantedSecurePermissions
import kotlin.math.abs

@Composable
fun HomeScreen(
    navigationIcon: @Composable () -> Unit,
) {
    val channel = remember { Channel<Boolean>() }
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberPermissionFlowRequestLauncher { isGranted ->
        coroutineScope.launch {
            channel.send(isGranted.values.all { it })
        }
    }
    val context = LocalContext.current

    suspend fun requestPermissions(permissions: List<Permission>): Boolean {
        val nonGrantedSecurePermissions = getNonGrantedSecurePermissions(
                context,
                permissions.filterIsInstance<Permission.SecurePermission>()
        )
        if (nonGrantedSecurePermissions.isNotEmpty()) {
            // do not request secure permissions directly, it would be confusing, so ask explicitly
            // instead
            return false
        }

        val normalPermissions = permissions.filterIsInstance<Permission.NormalPermission>()
            .map { it.id }.toTypedArray()
        if (checkPermissions(context, *normalPermissions)) {
            // permissions already granted
            return true
        }

        // some permission is not already granted, need to request it
        launcher.launch(normalPermissions)
        return channel.receive()
    }

    val viewModel: HomeScreenViewModel = hiltViewModel()
    // keep assigning permissionRequester at every recomposition because `launcher` changes when
    // the activity is recreated (no rememberSaveable is available)
    viewModel.skillEvaluator.permissionRequester = ::requestPermissions

    val enabledSkillsInfo = viewModel.skillHandler.enabledSkillsInfo.collectAsState()
    val interactionsState = viewModel.skillEvaluator.state.collectAsState()
    val sttState = viewModel.sttInputDevice.uiState.collectAsState()
    val wakeState = viewModel.wakeDevice.state.collectAsState()

    HomeScreen(
        skillContext = viewModel.skillContext,
        skills = enabledSkillsInfo.value,
        interactionLog = interactionsState.value,
        sttState = sttState.value,
        onSttClick = {
            viewModel.sttInputDevice.onClick(viewModel.skillEvaluator::processInputEvent)
        },
        wakeState = wakeState.value,
        onWakeDownload = {
            viewModel.wakeDevice.download()
        },
        onWakeDisable = viewModel::disableWakeWord,
        onManualUserInput = {
            viewModel.skillEvaluator.processInputEvent(InputEvent.Final(listOf(Pair(it, 1.0f))))
        },
        navigationIcon = navigationIcon,
        snackbarHost = {
            SnackbarHost(hostState = viewModel.snackbarHostState)
        },
    )
}

@Composable
fun HomeScreen(
    skillContext: SkillContext,
    // will be null when skills have not been initialized yet
    skills: List<SkillInfo>?,
    interactionLog: InteractionLog,
    // if the STT state is null, it means the user disabled the STT
    sttState: SttState?,
    onSttClick: () -> Unit,
    wakeState: WakeState?,
    onWakeDownload: () -> Unit,
    onWakeDisable: () -> Unit,
    onManualUserInput: (String) -> Unit,
    navigationIcon: @Composable () -> Unit,
    snackbarHost: @Composable () -> Unit,
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
                        contentDescription = stringResource(R.string.text_input_hint),
                    )
                },
                navigationIcon = navigationIcon,
            )
        },
        content = { paddingValues ->
            InteractionList(
                skillContext = skillContext,
                skills = skills,
                interactionLog = interactionLog,
                onConfirmedQuestionClick = { searchString = it },
                wakeState = wakeState,
                onWakeDownload = onWakeDownload,
                onWakeDisable = onWakeDisable,
                modifier = Modifier.padding(paddingValues),
            )
        },
        floatingActionButton = {
            // if the STT state is null, it means the user disabled the STT
            if (sttState != null) {
                SttFab(
                    state = sttState,
                    onClick = onSttClick,
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = snackbarHost,
    )
}

@Preview
@Composable
private fun HomeScreenPreview(@PreviewParameter(InteractionLogPreviews::class) interactionLog: InteractionLog) {
    val sttStatesPreviews = remember { SttStatesPreviews().values.toList() }
    var i by remember { mutableIntStateOf(abs(interactionLog.hashCode())) }

    AppTheme {
        HomeScreen(
            skillContext = SkillContextImpl.newForPreviews(LocalContext.current),
            skills = SkillInfoPreviews().values.toList(),
            interactionLog = interactionLog,
            sttState = sttStatesPreviews[i % sttStatesPreviews.size],
            onSttClick = { i += 1 },
            wakeState = null,
            onWakeDownload = {},
            onWakeDisable = {},
            onManualUserInput = {},
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                    )
                }
            },
            snackbarHost = {},
        )
    }
}
