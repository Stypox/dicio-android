package org.dicio.skill.output

import org.dicio.skill.util.CleanableUp

interface SpeechOutputDevice : CleanableUp {
    /**
     * Speaks the provided text
     * @param speechOutput what to tell to the user
     */
    fun speak(speechOutput: String)

    /**
     * If the device is currently speaking, it is stopped
     */
    fun stopSpeaking()

    /**
     * @return whether this device is currently speaking. Always returns false if a device has no
     * (well-defined) way to check if it is currently speaking (e.g. when it is just
     * displaying text).
     */
    val isSpeaking: Boolean

    /**
     * Calls the provided runnable when this device has finished speaking, if it is currently
     * speaking (i.e. [.isSpeaking] is true), or instantly otherwise. Can be called
     * multiple times with multiple runnables and they will all be run as explained. Once the
     * device has finished speaking, all of the previously added runnables will be removed.
     * @param runnable the runnable to execute when this device has finished speaking
     */
    fun runWhenFinishedSpeaking(runnable: Runnable)
}
