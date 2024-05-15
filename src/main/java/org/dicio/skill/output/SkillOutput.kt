package org.dicio.skill.output

import androidx.compose.runtime.Composable
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext

interface SkillOutput {
    fun getSpeechOutput(ctx: SkillContext): String

    fun getNextSkills(ctx: SkillContext): List<Skill> = listOf()

    @Composable
    fun GraphicalOutput(ctx: SkillContext)
}
