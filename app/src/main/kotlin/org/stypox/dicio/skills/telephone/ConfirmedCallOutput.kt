package org.stypox.dicio.skills.telephone

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.Headline

class ConfirmedCallOutput(
    context: Context,
    val number: String?,
) : SkillOutput {
    override val speechOutput = if (number == null) {
        context.getString(R.string.skill_telephone_not_calling)
    } else {
        "" // do not speak anything since a call has just started
    }

    @Composable
    override fun GraphicalOutput() {
        Headline(
            text = if (number == null) {
                stringResource(R.string.skill_telephone_not_calling)
            } else {
                stringResource(R.string.skill_telephone_calling, number)
            }
        )
    }
}
