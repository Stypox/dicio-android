package org.stypox.dicio.skills.timer

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.fragment.app.Fragment
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.timer
import org.stypox.dicio.SectionsGenerated.util_yes_no
import org.stypox.dicio.sentences.Sentences

object TimerInfo : SkillInfo("timer") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_timer)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_timer)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Timer)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sentences.Timer[ctx.locale.language] != null
                && Sentences.UtilYesNo[ctx.locale.language] != null
                && ctx.parserFormatter != null
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return TimerSkill(TimerInfo, Sentences.Timer[ctx.locale.language]!!)
    }
}
