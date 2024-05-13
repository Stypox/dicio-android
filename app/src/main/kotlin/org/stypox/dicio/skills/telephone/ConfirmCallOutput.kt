package org.stypox.dicio.skills.telephone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import org.stypox.dicio.output.graphical.Body
import org.stypox.dicio.output.graphical.Headline
import org.stypox.dicio.util.getString

class ConfirmCallOutput(
    private val name: String,
    private val number: String
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String =
        ctx.getString(R.string.skill_telephone_confirm_call, name)

    override fun getNextSkills(ctx: SkillContext): List<Skill> = listOf(
        ChainSkill.Builder(StandardRecognizer(Sections.getSection(SectionsGenerated.util_yes_no)))
            .output(ConfirmCallGenerator(number))
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
