package org.stypox.dicio.skills.calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.io.graphical.Subtitle
import org.stypox.dicio.util.getString

class CalculatorOutput(
    private val result: String?,
    private val spokenResult: String,
    private val inputInterpretation: String,
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext) = if (result == null) {
        ctx.getString(R.string.skill_calculator_could_not_calculate)
    } else {
        spokenResult
    }

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        if (result == null) {
            Headline(text = getSpeechOutput(ctx))
        } else {
            Column {
                Subtitle(text = inputInterpretation)
                Headline(text = result)
            }
        }
    }
}
