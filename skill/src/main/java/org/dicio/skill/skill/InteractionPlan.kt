package org.dicio.skill.skill

sealed interface InteractionPlan {
    /**
     * Whether to start the microphone after the speech output from [SkillOutput.getSpeechOutput]
     * finishes being spoken
     */
    val reopenMicrophone: Boolean

    /**
     * Considers the current interaction as finished, resetting the skill evaluator to the state it
     * was when the app was opened, i.e. removes all `nextSkills` from the stack of batches
     */
    data object FinishInteraction : InteractionPlan {
        override val reopenMicrophone = false
    }

    /**
     * Removes the top batch of `nextSkills` but keeps the rest of the stack of batches intact
     */
    data class FinishSubInteraction(
        override val reopenMicrophone: Boolean
    ) : InteractionPlan

    /**
     * Continues with the current batches of `nextSkills`, without modifying the stack of batches
     */
    data class Continue(
        override val reopenMicrophone: Boolean
    ) : InteractionPlan

    /**
     * Removes the `nextSkills` at the top of the stack of batches (if there is any) and adds
     * [nextSkills]
     */
    data class ReplaceSubInteraction(
        override val reopenMicrophone: Boolean,
        val nextSkills: List<Skill<*>>
    ) : InteractionPlan

    /**
     * Adds [nextSkills] to the stack of batches, effectively starting a new (sub-) interaction
     * because the `nextSkills` at the top of the stack have priority over others
     */
    data class StartSubInteraction(
        override val reopenMicrophone: Boolean,
        val nextSkills: List<Skill<*>>
    ) : InteractionPlan
}