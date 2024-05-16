package org.stypox.dicio.io.graphical

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.util.PermissionUtils
import org.stypox.dicio.util.getString

class MissingPermissionsSkillOutput(
    private val skill: SkillInfo
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String =
        ctx.getString(
            R.string.eval_missing_permissions,
            ctx.getString(skill.nameResource),
            PermissionUtils.getCommaJoinedPermissions(ctx.android, skill)
        )

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = skill.iconResource),
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
