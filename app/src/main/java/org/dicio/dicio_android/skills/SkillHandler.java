package org.dicio.dicio_android.skills;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.skills.fallback.text.TextFallbackInfo;
import org.dicio.dicio_android.skills.lyrics.LyricsInfo;
import org.dicio.dicio_android.skills.open.OpenInfo;
import org.dicio.dicio_android.skills.search.SearchInfo;
import org.dicio.dicio_android.skills.weather.WeatherInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SkillHandler {

    private static final List<SkillInfo> skillInfoList = new ArrayList<SkillInfo>() {{
        add(new WeatherInfo());
        add(new SearchInfo());
        add(new LyricsInfo());
        add(new OpenInfo());
    }};

    private static final List<SkillInfo> fallbackSkillInfoList = new ArrayList<SkillInfo>() {{
        add(new TextFallbackInfo());
    }};


    public static String getIsEnabledPreferenceKey(final String skillId) {
        return "skills_handler_is_enabled_" + skillId;
    }

    public static List<Skill> getStandardSkillBatch(
            final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final List<Skill> result = new ArrayList<>();

        for (final SkillInfo skillInfo : skillInfoList) {
            if (prefs.getBoolean(getIsEnabledPreferenceKey(skillInfo.getId()), true)) {
                result.add(skillInfo.build(context, prefs));
            }
        }

        return result;
    }

    public static Skill getFallbackSkill(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Objects.requireNonNull(fallbackSkillInfoList.get(0))
                .build(context, prefs);
    }

    public static List<SkillInfo> getSkillInfoList() {
        return skillInfoList;
    }
}
