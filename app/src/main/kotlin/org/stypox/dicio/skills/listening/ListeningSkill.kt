package org.stypox.dicio.skills.listening

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Listening

class ListeningSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Listening>) :
    StandardRecognizerSkill<Listening>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Listening): SkillOutput {
        val newEnable = when (inputData) {
            is Listening.Start -> true
            is Listening.Stop -> false
        }
        if (ctx.settingsAccess.wakeDeviceEnabled != newEnable) {
            ctx.settingsAccess.wakeDeviceEnabled = newEnable
        }
        return ListeningOutput(ctx.settingsAccess.wakeDeviceEnabled, newEnable)
    }
}
