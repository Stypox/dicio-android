package org.stypox.dicio

import android.content.Intent
import android.content.Intent.ACTION_ASSIST
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.stypox.dicio.di.LocaleManager
import org.stypox.dicio.io.input.SttInputDevice
import org.stypox.dicio.ui.main.MainScreen
import org.stypox.dicio.ui.nav.AppBarDrawerIcon
import org.stypox.dicio.ui.theme.AppTheme
import java.time.Instant
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivityCompose : ComponentActivity() {

    var sttInputDevice: SttInputDevice? = null
        @Inject set

    private var nextAssistAllowed = Instant.MIN

    /**
     * Automatically loads the LLM and the STT when the [ACTION_ASSIST] intent is received. Applies
     * a backoff of [INTENT_BACKOFF_MILLIS], since during testing Android would send the assist
     * intent to the app twice in a row.
     */
    private fun onAssistIntentReceived() {
        val now = Instant.now()
        if (nextAssistAllowed < now) {
            nextAssistAllowed = now.plusMillis(INTENT_BACKOFF_MILLIS)
            Log.d(TAG, "Received assist intent")
            sttInputDevice?.tryLoad(thenStartListening = true)
        } else {
            Log.w(TAG, "Ignoring duplicate assist intent")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == ACTION_ASSIST) {
            onAssistIntentReceived()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == ACTION_ASSIST) {
            onAssistIntentReceived()
        }

        setContent {
            AppTheme {
                DrawerWithScreen()
            }
        }
    }

    companion object {
        private const val INTENT_BACKOFF_MILLIS = 100L
        private val TAG = MainActivityCompose::class.simpleName
    }
}

@Preview
@Composable
fun DrawerWithScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
