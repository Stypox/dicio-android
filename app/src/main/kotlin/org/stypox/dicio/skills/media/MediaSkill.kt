package org.stypox.dicio.skills.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSessionManager
import android.view.KeyEvent
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
        val mediaSessionManager =
            (ctx.android.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager?)
        val activeSessions = mediaSessionManager?.getActiveSessions(notificationListener)
        if (activeSessions?.isNotEmpty() == true) {
            val key = when (inputData) {
                is Media.Play -> KeyEvent.KEYCODE_MEDIA_PLAY
                is Media.Pause -> KeyEvent.KEYCODE_MEDIA_PAUSE
                is Media.Previous -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
                is Media.Next -> KeyEvent.KEYCODE_MEDIA_NEXT
            }
            activeSessions[0].apply {
                dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_DOWN, key))
                dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_UP, key))
            }
        }

        return MediaOutput(
            command = inputData
        )
    }

    companion object {
        val TAG: String = MediaSkill::class.simpleName!!
    }
}
