package org.stypox.dicio.io.input.stt_service

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionService
import android.speech.SpeechRecognizer
import dagger.hilt.android.AndroidEntryPoint
import org.stypox.dicio.di.SttInputDeviceWrapper
import org.stypox.dicio.io.input.InputEvent
import javax.inject.Inject


@AndroidEntryPoint
class SttService : RecognitionService() {

    @Inject
    lateinit var sttInputDevice: SttInputDeviceWrapper

    override fun onStartListening(recognizerIntent: Intent, listener: Callback) {
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
                    listener.error(SpeechRecognizer.ERROR_NO_MATCH)
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
            // TODO choose better error to indicate that manual intervention is required to
            //  download the Vosk model
            listener.error(SpeechRecognizer.ERROR_NETWORK)
        }
    }

    override fun onCancel(listener: Callback) {
        sttInputDevice.stopListening()
    }

    override fun onStopListening(listener: Callback) {
        sttInputDevice.stopListening()
    }
}
