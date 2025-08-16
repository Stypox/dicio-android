package org.stypox.dicio.skills.notify

import androidx.compose.runtime.Composable
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.util.getString

data class NotifyOutput(val message: String, val appName: String): SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String {
        return ctx.getString(R.string.skill_notify_message, appName)

    }

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        TODO("Not yet implemented")
    }
}