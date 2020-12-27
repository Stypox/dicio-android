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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SkillHandler {

    private static final Map<String, SkillInfo> skillInfoMap =
            new HashMap<String, SkillInfo>() {{
                put("weather", new WeatherInfo());
                put("search", new SearchInfo());
                put("lyrics", new LyricsInfo());
                put("open", new OpenInfo());
            }};

    private static final Map<String, SkillInfo> fallbackSkillInfoMap =
            new HashMap<String, SkillInfo>() {{
                put("text", new TextFallbackInfo());
            }};


    public static String getIsEnabledPreferenceKey(final String skillId) {
        return "skills_handler_is_enabled_" + skillId;
    }

    public static List<Skill> getStandardSkillBatch(
            final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final List<Skill> result = new ArrayList<>();

        for (Map.Entry<String, SkillInfo> entry
                : skillInfoMap.entrySet()) {
            if (prefs.getBoolean(getIsEnabledPreferenceKey(entry.getKey()), true)) {
                result.add(entry.getValue().build(context, prefs));
            }
        }

        return result;
    }

    public static Skill getFallbackSkill(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Objects.requireNonNull(fallbackSkillInfoMap.get("text"))
                .build(context, prefs);
    }
}
