package org.stypox.dicio.io.graphical

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        Text(text = getSpeechOutput(ctx))
    }
}
