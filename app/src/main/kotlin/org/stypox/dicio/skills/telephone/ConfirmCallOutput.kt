package org.stypox.dicio.skills.telephone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.Body
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.util.RecognizeYesNoSkill
import org.stypox.dicio.util.getString

class ConfirmCallOutput(
    private val name: String,
    private val number: String
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String =
        ctx.getString(R.string.skill_telephone_confirm_call, name)

    override fun getNextSkills(ctx: SkillContext): List<Skill<*>> = listOf(
        object : RecognizeYesNoSkill(TelephoneInfo, Sentences.UtilYesNo[ctx.sentencesLanguage]!!) {
            override suspend fun generateOutput(ctx: SkillContext, inputData: Boolean): SkillOutput {
                return if (inputData) {
                    TelephoneSkill.call(ctx.android, number)
                    ConfirmedCallOutput(number)
                } else {
                    ConfirmedCallOutput(null)
                }
            }
        }
    )

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        Column {
            Headline(text = getSpeechOutput(ctx))
            Spacer(modifier = Modifier.height(4.dp))
            Body(text = number)
        }
    }
}
