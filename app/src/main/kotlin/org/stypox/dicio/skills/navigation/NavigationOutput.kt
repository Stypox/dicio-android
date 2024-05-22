package org.stypox.dicio.skills.navigation

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.getString

class NavigationOutput(
    private val where: String?,
) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = if (where.isNullOrBlank()) {
        ctx.getString(R.string.skill_navigation_specify_where)
    } else {
        ctx.getString(R.string.skill_navigation_navigating_to, where)
    }
}
