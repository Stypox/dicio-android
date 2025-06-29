package org.stypox.dicio.skills.listening

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.getString

class ListeningOutput(
    private val isWakeWordEnabled: Boolean,
    private val previouslyRunning: Boolean,
    private val shouldBeRunning: Boolean,
) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = if (!isWakeWordEnabled) {
        ctx.getString(R.string.skill_listening_disabled)
    } else if (previouslyRunning) {
        if (shouldBeRunning) {
            ctx.getString(R.string.skill_listening_already_listening)
        } else {
            ctx.getString(R.string.skill_listening_stop_listening)
        }
    } else {
        if (shouldBeRunning) {
            ctx.getString(R.string.skill_listening_started_listening)
        } else {
            ctx.getString(R.string.skill_listening_not_listening)
        }
    }
}
