package org.stypox.dicio.skills.notify

import android.Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Watch
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Permission
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.util.PERMISSION_CALL_PHONE
import org.stypox.dicio.util.PERMISSION_READ_CONTACTS

object NotifyInfo: SkillInfo("notify") {
    override fun name(context: Context) =
        R.string.skill_name_notify.toString()

    override fun sentenceExample(context: Context): String =
        R.string.skill_sentence_example_notify.toString()

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Watch)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sentences.Notify[ctx.sentencesLanguage] != null

    }

    override val neededPermissions: List<Permission>
            = listOf()

    override fun build(ctx: SkillContext): Skill<*> {
        return NotifySkill(NotifyInfo, Sentences.Notify[ctx.sentencesLanguage]!!)
    }
}