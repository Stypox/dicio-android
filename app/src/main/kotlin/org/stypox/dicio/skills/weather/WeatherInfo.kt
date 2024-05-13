package org.stypox.dicio.skills.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.weather

class WeatherInfo : SkillInfo(
    "weather", R.string.skill_name_weather, R.string.skill_sentence_example_weather,
    R.drawable.ic_cloud_white, true
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(weather)
    }

    override fun build(context: SkillContext): Skill {
        return ChainSkill.Builder(StandardRecognizer(Sections.getSection(weather)))
            .process(OpenWeatherMapProcessor())
            .output(WeatherGenerator())
    }

    override val preferenceFragment: Fragment
        get() = Preferences()

    class Preferences : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_weather)
            val keyDefaultCity: String = getString(R.string.pref_key_weather_default_city)
            findPreference<Preference>(keyDefaultCity)?.summaryProvider =
                Preference.SummaryProvider<Preference> {
                    val value = preferenceManager.sharedPreferences?.getString(keyDefaultCity, null)
                    if (value?.trim { it <= ' ' }.isNullOrEmpty()) {
                        getString(R.string.pref_weather_default_city_using_ip_info)
                    } else {
                        value
                    }
                }
        }
    }
}
