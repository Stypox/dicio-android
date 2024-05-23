package org.stypox.dicio.skills.weather

import android.content.Context
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.weather
import org.stypox.dicio.settings.ui.SettingsItem

object WeatherInfo : SkillInfo("weather") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_weather)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_weather)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Cloud)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sections.isSectionAvailable(weather)
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return WeatherSkill(WeatherInfo, Sections.getSection(weather))
    }

    override val renderSettings: @Composable () -> Unit get() = @Composable {
        // TODO actually implement setting
        SettingsItem(
            title = stringResource(R.string.pref_weather_default_city),
            description = stringResource(R.string.pref_weather_default_city_using_ip_info),
        )
    }
}
