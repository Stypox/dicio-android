package org.stypox.dicio.output.graphical

import androidx.compose.runtime.Composable
import org.dicio.skill.SkillContext
import org.dicio.skill.output.SkillOutput

/**
 * A [SkillOutput] where the graphical output is just a headline text with the speech output.
 */
interface HeadlineSpeechSkillOutput : SkillOutput {
    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        Headline(text = getSpeechOutput(ctx))
    }
}
