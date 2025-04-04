package org.stypox.dicio.skills.listening

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.io.wake.WakeService
import org.stypox.dicio.sentences.Sentences.Listening

class ListeningSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Listening>) :
    StandardRecognizerSkill<Listening>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Listening): SkillOutput {
        val wasRunning = WakeService.isRunning()
        val shouldBeRunning = when (inputData) {
            is Listening.Start -> true
            is Listening.Stop -> false
        }
        if (shouldBeRunning) {
            WakeService.start(ctx.android)
        } else {
            WakeService.stop(ctx.android)
        }
        return ListeningOutput(wasRunning, shouldBeRunning)
    }
}
