package org.stypox.dicio.io.graphical

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.util.PermissionUtils
import org.stypox.dicio.util.getString

class MissingSecureSettingsSkillOutput(
    private val skill: SkillInfo
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String =
        ctx.getString(
            R.string.eval_missing_permissions,
            skill.name(ctx.android),
            PermissionUtils.getCommaJoinedSecureSettings(skill)
        )

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = skill.icon(),
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp)
            )
            Text(
                text = getSpeechOutput(ctx),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}
