package org.stypox.dicio.output.speech

import org.dicio.skill.output.SpeechOutputDevice

/**
 * A speech device that speaks instantaneously (e.g. because it just displays text). Overrides
 * [isSpeaking] so that it always returns false, and [runWhenFinishedSpeaking] so that the runnable is run instantly.
 */
abstract class InstantSpeechDevice : SpeechOutputDevice {
    override fun isSpeaking(): Boolean {
        return false
    }

    override fun runWhenFinishedSpeaking(runnable: Runnable) {
        runnable.run()
    }
}