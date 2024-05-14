package org.stypox.dicio.ui.main

import org.dicio.skill.SkillInfo

data class PendingQuestion(
    val userInput: String,
    val continuesLastConversation: Boolean,
    val skillBeingEvaluated: SkillInfo?,
)
