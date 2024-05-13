package org.stypox.dicio.skills.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.Headline

class NavigationOutput(
    context: Context,
    val where: String?,
) : SkillOutput {
    override val speechOutput = if (where.isNullOrBlank()) {
        context.getString(R.string.skill_navigation_specify_where)
    } else {
        context.getString(R.string.skill_navigation_navigating_to, where)
    }

    @Composable
    override fun GraphicalOutput() {
        Headline(text = speechOutput)
    }
}
