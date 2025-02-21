package org.stypox.dicio.skills.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSessionManager
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Media

class MediaSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Media>)
    : StandardRecognizerSkill<Media>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Media): SkillOutput {
        val notificationListener = ComponentName(ctx.android, MediaNotificationListener::class.java)
        val mediaSessionManager = getSystemService(ctx.android, MediaSessionManager::class.java)
        val activeSession = mediaSessionManager
            ?.getActiveSessions(notificationListener)
            ?.firstOrNull()
            ?: return MediaOutput(performedAction = null) // no media session found

        val key = when (inputData) {
            is Media.Play -> KeyEvent.KEYCODE_MEDIA_PLAY
            is Media.Pause -> KeyEvent.KEYCODE_MEDIA_PAUSE
            is Media.Previous -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            is Media.Next -> KeyEvent.KEYCODE_MEDIA_NEXT
        }

        // if any of these return false then there is no media session
        val sentDown = activeSession.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_DOWN, key))
        val sentUp = activeSession.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_UP, key))
        return MediaOutput(performedAction = if (sentDown && sentUp) inputData else null)
    }

    companion object {
        val TAG: String = MediaSkill::class.simpleName!!
    }
}
