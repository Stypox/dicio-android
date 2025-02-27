package org.stypox.dicio.skills.fallback.text

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.AlwaysWorstScore
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.skill.Specificity
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.getString

class TextFallbackOutput(
    val askToRepeat: Boolean
) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
        if (askToRepeat) R.string.eval_no_match_repeat
        else R.string.eval_no_match
    )

    // this makes it so that the evaluator will open the microphone again, but the skill provided
    // will never actually match, so the previous batch of skills will be used instead
    override fun getNextSkills(ctx: SkillContext): List<Skill<*>> =
        if (askToRepeat)
            listOf(
                object : Skill<Unit>(TextFallbackInfo, Specificity.LOW) {
                    override fun score(ctx: SkillContext, input: String) = Pair(AlwaysWorstScore, Unit)

                    override suspend fun generateOutput(
                        ctx: SkillContext,
                        inputData: Unit
                    ): SkillOutput {
                        // impossible situation, since AlwaysWorstScore was used above
                        throw RuntimeException("AlwaysWorstScore still triggered generateOutput")
                    }
                }
            )
        else
            listOf()
}
