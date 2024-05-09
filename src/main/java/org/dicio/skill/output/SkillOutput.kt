package org.dicio.skill.output

import androidx.compose.runtime.Composable
import org.dicio.skill.Skill

interface SkillOutput {
    val speechOutput: String

    val nextSkills: List<Skill>
        get() = listOf()

    @Composable
    fun GraphicalOutput()
}
