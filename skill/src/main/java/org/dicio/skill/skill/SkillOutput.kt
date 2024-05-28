package org.dicio.skill.skill

import androidx.compose.runtime.Composable
import org.dicio.skill.context.SkillContext

interface SkillOutput {
    fun getSpeechOutput(ctx: SkillContext): String

    fun getNextSkills(ctx: SkillContext): List<Skill<*>> = listOf()

    @Composable
    fun GraphicalOutput(ctx: SkillContext)
}
