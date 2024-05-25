package org.stypox.dicio.io.input.stt_service

import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import org.stypox.dicio.util.BaseComposeActivity

@AndroidEntryPoint
class SttServiceActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var customHint: String? = null
        var startedForSpeechResult = false
        var speechExtras: Bundle? = null

        if (intent != null && RecognizerIntent.ACTION_RECOGNIZE_SPEECH == intent.action) {
            startedForSpeechResult = true
            speechExtras = intent.extras
            speechExtras?.also { bundle ->
                customHint = bundle.getString(RecognizerIntent.EXTRA_PROMPT, null)
            }
        }

        composeSetContent {
            SttServiceBottomSheet(
                customHint = customHint,
                onDoneClicked = if (startedForSpeechResult) { utterancesWithConfidence ->
                    sendSpeechResult(
                        utterances = utterancesWithConfidence,
                        maybeSpeechExtras = speechExtras,
                    )
                } else {
                    null
                },
                onDismissRequest = {
                    finish()
                }
            )
        }
    }

    private fun sendSpeechResult(
        utterances: List<Pair<String, Float>>,
        maybeSpeechExtras: Bundle?,
    ) {
        // calculate how many items to return
        val maxResults = maybeSpeechExtras?.getInt(RecognizerIntent.EXTRA_MAX_RESULTS)
        val filteredUtterances = if (maxResults == null || maxResults > utterances.size) {
            utterances // keep all utterances provided by the STT device
        } else {
            utterances.take(maxResults) // only keep the best maxResults
        }

        // get results from recognizer and prepare for reporting
        val foundTexts = ArrayList(filteredUtterances.map { it.first })
        val confidenceScore = filteredUtterances.map { it.second }.toFloatArray()

        // Prepare Result Intent with Extras
        val intent = Intent()
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, foundTexts)
        intent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, confidenceScore)

        // This is for some apps, who use the Android SearchManager (e.g. ebay)
        // (in my eyes probably wrong implemented by them, however without it's not working...)
        intent.putExtra(SearchManager.QUERY, utterances[0].first)
        setResult(RESULT_OK, intent)

        // send pending intent result, if needed
        val speechExtras = maybeSpeechExtras ?: return
        if (speechExtras.containsKey(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT)) {
            if (speechExtras.containsKey(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE)) {
                intent.extras?.putAll(speechExtras.getBundle(
                    RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE
                ))
            }
            val resultIntent = speechExtras.getParcelable<PendingIntent>(
                RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT
            )
            try {
                resultIntent?.send(this, RESULT_OK, intent)
            } catch (e: CanceledException) {
                Log.w(TAG, "Speech result pending intent canceled", e)
            }
        }
    }

    companion object {
        private val TAG = SttServiceActivity::class.java.simpleName
    }
}
