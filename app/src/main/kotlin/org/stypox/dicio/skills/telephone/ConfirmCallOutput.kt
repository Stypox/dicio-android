package org.stypox.dicio.skills.telephone

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import org.stypox.dicio.output.graphical.Body
import org.stypox.dicio.output.graphical.Headline

class ConfirmCallOutput(
    context: Context,
    name: String,
    val number: String
) : SkillOutput {
    override val speechOutput = context.getString(R.string.skill_telephone_confirm_call, name)

    override val nextSkills = listOf(
        ChainSkill.Builder(StandardRecognizer(Sections.getSection(SectionsGenerated.util_yes_no)))
            .output(ConfirmCallGenerator(number))
    )

    @Composable
    override fun GraphicalOutput() {
        Column {
            Headline(text = speechOutput)
            Spacer(modifier = Modifier.height(4.dp))
            Body(text = number)
        }
    }
}
