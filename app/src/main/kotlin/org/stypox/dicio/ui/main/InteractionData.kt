package org.stypox.dicio.ui.main

import org.dicio.skill.SkillInfo
import org.dicio.skill.output.SkillOutput

data class Interaction(
    val skill: SkillInfo?,
    val questionsAnswers: List<Pair<String, SkillOutput>>
)

data class PendingQuestion(
    val userInput: String,
    val continuesLastInteraction: Boolean,
    val skillBeingEvaluated: SkillInfo?,
)

data class InteractionLog(
    val interactions: List<Interaction>,
    val pendingQuestion: PendingQuestion?,
)
