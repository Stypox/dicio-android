package org.stypox.dicio.error

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The user actions that can cause an error.
 * @implNote Taken with some modifications from NewPipe, file error/UserAction.java
 */
@Parcelize
enum class UserAction(val message: String) : Parcelable {
    STT_SERVICE_SPEECH_TO_TEXT("Stt service speech to text"),
    SKILL_EVALUATION("Skill evaluation");
}