package org.stypox.dicio.skills.notify

import android.content.Context
import androidx.compose.runtime.Composable
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo

object NotifyInfo: SkillInfo("notify") {
    override fun name(context: Context) =
        "notify"

    override fun sentenceExample(context: Context): String =
        "What was my notification?"

    @Composable
    override fun icon() =
        TODO()

    override fun isAvailable(ctx: SkillContext): Boolean {
        TODO()
    }

    override fun build(ctx: SkillContext): Skill<*> {
        TODO("Not yet implemented")
    }
}