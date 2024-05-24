package org.stypox.dicio

import android.content.Intent
import android.content.Intent.ACTION_ASSIST
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.stypox.dicio.di.LocaleManager
import org.stypox.dicio.io.input.InputEventsModule
import org.stypox.dicio.io.input.SttInputDevice
import org.stypox.dicio.ui.nav.Navigation
import org.stypox.dicio.ui.theme.AppTheme
import org.stypox.dicio.util.LocaleAwareActivity2
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class MainActivityCompose : LocaleAwareActivity2() {

    @Inject
    lateinit var inputEventsModule: InputEventsModule
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
            sttInputDevice?.tryLoad(inputEventsModule::tryEmitEvent)
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
        } else {
            // load the input device, without starting to listen
            sttInputDevice?.tryLoad(null)
        }

        enableEdgeToEdge()

        localeAwareSetContent {
            AppTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.safeDrawingPadding()
                    ) {
                        Navigation()
                    }
                }
            }
        }
    }

    companion object {
        private const val INTENT_BACKOFF_MILLIS = 100L
        private val TAG = MainActivityCompose::class.simpleName
    }
}
