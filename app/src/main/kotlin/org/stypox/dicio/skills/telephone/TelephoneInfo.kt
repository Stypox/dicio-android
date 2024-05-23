package org.stypox.dicio.skills.telephone

import android.Manifest
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.fragment.app.Fragment
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.telephone
import org.stypox.dicio.SectionsGenerated.util_yes_no

object TelephoneInfo : SkillInfo("open") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_telephone)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_telephone)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Call)

    override val neededPermissions: List<String>
            = listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sections.isSectionAvailable(telephone) && Sections.isSectionAvailable(util_yes_no)
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return TelephoneSkill(TelephoneInfo, Sections.getSection(telephone))
    }
}
