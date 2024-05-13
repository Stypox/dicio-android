package org.stypox.dicio.skills.fallback.text

import android.content.Context
import androidx.compose.runtime.Composable
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.Headline

class TextFallbackOutput(
    context: Context
) : SkillOutput {
    override val speechOutput = context.getString(R.string.eval_no_match)

    @Composable
    override fun GraphicalOutput() {
        Headline(text = speechOutput)
    }
}
