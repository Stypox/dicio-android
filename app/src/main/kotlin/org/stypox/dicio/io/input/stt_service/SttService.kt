package org.stypox.dicio.io.input.stt_service

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.AndroidEntryPoint
import org.stypox.dicio.di.LocaleManager
import org.stypox.dicio.di.SttInputDeviceWrapper
import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.util.LocaleUtils
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class SttService : RecognitionService() {

    @Inject
    lateinit var sttInputDevice: SttInputDeviceWrapper

    @Inject
    lateinit var localeManager: LocaleManager

    override fun onStartListening(recognizerIntent: Intent, listener: Callback) {
        val wantedLanguageExtra = recognizerIntent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE)
        // "und" is "Undetermined", see https://www.loc.gov/standards/iso639-2/php/code_list.php
        if (wantedLanguageExtra != null && wantedLanguageExtra != "und") {
            val appLanguage = localeManager.locale.value.language
            val wantedLanguage = Locale(wantedLanguageExtra).language
            if (appLanguage != wantedLanguage) {
                Log.e(TAG, "Unsupported language: app=$appLanguage wanted=$wantedLanguageExtra")
                // From the javadoc of ERROR_LANGUAGE_UNAVAILABLE: Requested language is supported,
                // but not available currently (e.g. not downloaded yet).
                listener.error(ERROR_LANGUAGE_UNAVAILABLE)
                return
            }
        }

        var beginningOfSpeech = true
        val willStartListening = sttInputDevice.tryLoad { inputEvent ->
            when (inputEvent) {
                is InputEvent.Error -> {
                    listener.error(SpeechRecognizer.ERROR_SERVER)
                }
                is InputEvent.Final -> {
                    val results = Bundle()
                    results.putStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION,
                        ArrayList(inputEvent.utterances.map { it.first })
                    )
                    results.putFloatArray(
                        SpeechRecognizer.CONFIDENCE_SCORES,
                        inputEvent.utterances.map { it.second }.toFloatArray()
                    )
                    listener.results(results)
                    listener.endOfSpeech()
                }
                InputEvent.None -> {
                    listener.error(SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
                    listener.endOfSpeech()
                }
                is InputEvent.Partial -> {
                    if (beginningOfSpeech) {
                        listener.beginningOfSpeech()
                        beginningOfSpeech = false
                    }
                    val partResult = Bundle()
                    partResult.putStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION,
                        arrayListOf(inputEvent.utterance)
                    )
                    listener.partialResults(partResult)
                }
            }
        }

        if (!willStartListening) {
            listener.error(ERROR_LANGUAGE_UNAVAILABLE)
        }
    }

    override fun onCancel(listener: Callback) {
        sttInputDevice.stopListening()
    }

    override fun onStopListening(listener: Callback) {
        sttInputDevice.stopListening()
    }

    companion object {
        val TAG = SttService::class.simpleName

        /**
         * From the javadoc of [SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE]: Requested language is
         * supported, but not available currently (e.g. not downloaded yet).
         */
        val ERROR_LANGUAGE_UNAVAILABLE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE
        } else {
            SpeechRecognizer.ERROR_SERVER
        }
    }
}
