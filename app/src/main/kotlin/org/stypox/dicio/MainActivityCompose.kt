package org.stypox.dicio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.dicio.skill.SkillInfo
import org.stypox.dicio.eval.SkillEvaluator2
import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.io.input.InputEventsModule
import org.stypox.dicio.io.input.vosk.VoskInputDevice
import org.stypox.dicio.skills.SkillHandler
import org.stypox.dicio.ui.main.InteractionLog
import org.stypox.dicio.ui.main.MainScreen
import org.stypox.dicio.ui.nav.AppBarDrawerIcon
import org.stypox.dicio.ui.theme.AppTheme
import java.util.Locale

class MainActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                DrawerWithScreen()
            }
        }
    }
}

@Preview
@Composable
fun DrawerWithScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    remember {
        Sections.setLocale(LocaleListCompat.create(Locale.ENGLISH))
        SkillHandler.setSkillContextAndroidAndLocale(context)
        1
    }
    val inputEventsModule = remember {
        InputEventsModule()
    }
    val stt = remember {
        VoskInputDevice(context, OkHttpClient.Builder().build(), inputEventsModule)
    }
    val skillEvaluator = remember {
        SkillEvaluator2(inputEventsModule, SkillHandler.skillContext) { true }
    }
    val interactionsState by skillEvaluator.state.collectAsState()
    val sttState by stt.uiState.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Drawer title", modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item") },
                    selected = false,
                    onClick = {

                    }
                )
            }
        }
    ) {
        MainScreen(
            interactionLog = interactionsState,
            sttState = sttState,
            onSttClick = stt::onClick,
            onManualUserInput = { inputEventsModule.tryEmitEvent(InputEvent.Final(listOf(it))) },
            navigationIcon = {
                AppBarDrawerIcon(
                    onDrawerClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    isClosed = drawerState.isClosed,
                )
            }
        )
    }
}
