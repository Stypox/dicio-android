package org.stypox.dicio.skills.calendar

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import kotlinx.coroutines.launch
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.settings.ui.ListSetting
import org.stypox.dicio.settings.ui.StringSetting

object CalendarInfo : SkillInfo("calendar") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_calendar)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_calendar)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.CalendarMonth)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sentences.Calendar[ctx.sentencesLanguage] != null && ctx.parserFormatter != null
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return CalendarSkill(CalendarInfo, Sentences.Calendar[ctx.sentencesLanguage]!!)
    }
}
