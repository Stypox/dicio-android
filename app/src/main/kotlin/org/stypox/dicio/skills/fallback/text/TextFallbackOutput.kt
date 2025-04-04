package org.stypox.dicio.skills.fallback.text

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.InteractionPlan
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
    override fun getInteractionPlan(ctx: SkillContext) =
        InteractionPlan.Continue(reopenMicrophone = askToRepeat)
}
