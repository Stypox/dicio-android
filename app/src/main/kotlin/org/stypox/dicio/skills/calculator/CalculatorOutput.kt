package org.stypox.dicio.skills.calculator

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.Headline
import org.stypox.dicio.output.graphical.Subtitle

class CalculatorOutput(
    context: Context,
    private val result: String?,
    spokenResult: String,
    private val inputInterpretation: String,
) : SkillOutput {
    override val speechOutput = if (result == null) {
        context.getString(R.string.skill_calculator_could_not_calculate)
    } else {
        spokenResult
    }

    @Composable
    override fun GraphicalOutput() {
        if (result == null) {
            Headline(text = speechOutput)
        } else {
            Column {
                Subtitle(text = inputInterpretation)
                Headline(text = result)
            }
        }
    }
}
