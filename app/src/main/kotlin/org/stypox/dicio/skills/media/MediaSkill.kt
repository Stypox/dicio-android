package org.stypox.dicio.skills.media

import android.media.AudioManager
import android.view.KeyEvent
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
        val audioManager = getSystemService(ctx.android, AudioManager::class.java)
            ?: return MediaOutput(performedAction = null) // no media session found

        val key = when (inputData) {
            is Media.Play -> KeyEvent.KEYCODE_MEDIA_PLAY
            is Media.Pause -> KeyEvent.KEYCODE_MEDIA_PAUSE
            is Media.Previous -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            is Media.Next -> KeyEvent.KEYCODE_MEDIA_NEXT
        }

        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, key))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, key))
        return MediaOutput(performedAction = inputData)
    }

    companion object {
        val TAG: String = MediaSkill::class.simpleName!!
    }
}
