package org.stypox.dicio.skills.fallback.text

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.util.RecognizeEverythingSkill

class TextFallbackSkill(correspondingSkillInfo: SkillInfo) :
    RecognizeEverythingSkill(correspondingSkillInfo) {
    override suspend fun generateOutput(ctx: SkillContext, inputData: String): SkillOutput {
        return TextFallbackOutput()
    }
}
