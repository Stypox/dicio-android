package org.stypox.dicio.skills.current_time

import org.dicio.skill.SkillContext
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.getString

class CurrentTimeOutput(
    private val timeStr: String,
) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String =
        ctx.getString(R.string.skill_time_current_time, timeStr)
}
