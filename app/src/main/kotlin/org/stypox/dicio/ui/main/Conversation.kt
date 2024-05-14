package org.stypox.dicio.ui.main

import org.dicio.skill.SkillInfo
import org.dicio.skill.output.SkillOutput

data class Conversation(
    val skill: SkillInfo,
    val questionsAnswers: List<Pair<String, SkillOutput>>
)
