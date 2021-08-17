package org.dicio.dicio_android.skills.weather;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.Sections.isSectionAvailable;
import static org.dicio.dicio_android.SectionsGenerated.weather;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

public class WeatherInfo extends SkillInfo {

    public WeatherInfo() {
        super("weather", R.string.skill_name_weather, R.string.skill_sentence_example_weather,
                R.drawable.ic_cloud_white, true);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return isSectionAvailable(weather);
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder()
                .recognize(new StandardRecognizer(getSection(weather)))
                .process(new OpenWeatherMapProcessor())
                .output(new WeatherOutput());
    }

    @Nullable
    @Override
    public PreferenceFragmentCompat getPreferenceFragment() {
        return new Preferences();
    }

    public static class Preferences extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
            addPreferencesFromResource(R.xml.pref_weather);

            final String keyDefaultCity = getString(R.string.pref_key_weather_default_city);
            final Preference defaultCityPreference = findPreference(keyDefaultCity);
            assert defaultCityPreference != null;

            defaultCityPreference.setSummaryProvider(preference -> {
                final String value = getPreferenceManager().getSharedPreferences()
                        .getString(keyDefaultCity, null);
                if (value == null || value.trim().isEmpty()) {
                    return getString(R.string.pref_weather_default_city_using_ip_info);
                } else {
                    return value;
                }
            });
        }
    }
}
