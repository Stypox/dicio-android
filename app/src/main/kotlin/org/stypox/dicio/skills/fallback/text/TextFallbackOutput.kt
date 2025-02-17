package org.stypox.dicio.skills.fallback.text

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.getString

class TextFallbackOutput(private val secondTime: Boolean) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String =
        if (secondTime) {
            ctx.getString(R.string.eval_no_match_no_repeat)
        } else {
            ctx.getString(R.string.eval_no_match)
        }

    override fun getKeepListening(ctx: SkillContext): Boolean = !secondTime
}
