package org.stypox.dicio.output.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.annotation.StringRes
import org.dicio.skill.output.SpeechOutputDevice
import org.stypox.dicio.R
import java.util.Locale

class AndroidTtsSpeechDevice(private var context: Context, locale: Locale) : SpeechOutputDevice {
    private var textToSpeech: TextToSpeech? = null
    private var initializedCorrectly = false
    private val runnablesWhenFinished: MutableList<Runnable> = ArrayList()
    private var lastUtteranceId = 0

    init {
        textToSpeech = TextToSpeech(context, OnInitListener label@ { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.run {
                    if (setLanguage(locale) >= 0) { // errors are -1 or -2
                        initializedCorrectly = true
                        setOnUtteranceProgressListener(object :
                            UtteranceProgressListener() {
                            override fun onStart(utteranceId: String) {}
                            override fun onDone(utteranceId: String) {
                                if ("dicio_$lastUtteranceId" == utteranceId) {
                                    // run only when the last enqueued utterance is finished
                                    for (runnable in runnablesWhenFinished) {
                                        runnable.run()
                                    }
                                    runnablesWhenFinished.clear()
                                }
                            }

                            @Deprecated("")
                            override fun onError(utteranceId: String) {}
                        })
                    } else {
                        handleInitializationError(R.string.android_tts_unsupported_language)
                    }
                }
            } else {
                handleInitializationError(R.string.android_tts_error)
            }
        })
    }

    override fun speak(speechOutput: String) {
        if (initializedCorrectly) {
            lastUtteranceId += 1
            textToSpeech?.speak(
                speechOutput, TextToSpeech.QUEUE_ADD, null,
                "dicio_$lastUtteranceId"
            )
        } else {
            Toast.makeText(context, speechOutput, Toast.LENGTH_LONG).show()
        }
    }

    override fun stopSpeaking() {
        textToSpeech?.stop()
    }

    override fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }

    override fun runWhenFinishedSpeaking(runnable: Runnable) {
        if (isSpeaking) {
            runnablesWhenFinished.add(runnable)
        } else {
            runnable.run()
        }
    }

    override fun cleanup() {
        textToSpeech?.apply {
            shutdown()
            textToSpeech = null
        }
    }

    private fun handleInitializationError(@StringRes errorString: Int) {
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
        cleanup()
    }
}