package org.stypox.dicio.ui.home

import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput

data class QuestionAnswer(
    val question: String?,
    val answer: SkillOutput,
)

data class Interaction(
    // the second item of the pair is true if the skill is the fallback skill
    val skill: Pair<SkillInfo, Boolean>?,
    val questionsAnswers: List<QuestionAnswer>,
)

data class PendingQuestion(
    val userInput: String,
    val continuesLastInteraction: Boolean,
    val skillBeingEvaluated: Pair<SkillInfo, Boolean>?,
)

data class InteractionLog(
    val interactions: List<Interaction>,
    val pendingQuestion: PendingQuestion?,
)
