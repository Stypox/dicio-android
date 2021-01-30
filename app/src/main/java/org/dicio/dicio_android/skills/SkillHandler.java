package org.dicio.dicio_android.skills;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.DrawableRes;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.skills.fallback.text.TextFallbackInfo;
import org.dicio.dicio_android.skills.lyrics.LyricsInfo;
import org.dicio.dicio_android.skills.open.OpenInfo;
import org.dicio.dicio_android.skills.search.SearchInfo;
import org.dicio.dicio_android.skills.weather.WeatherInfo;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

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


    public static List<Skill> getStandardSkillBatch(final Context context, final Locale locale) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final List<Skill> result = new ArrayList<>();

        for (final SkillInfo skillInfo : skillInfoList) {
            if (prefs.getBoolean(getIsEnabledPreferenceKey(skillInfo.getId()), true)) {
                result.add(skillInfo.build(context, prefs, locale));
            }
        }

        return result;
    }

    public static Skill getFallbackSkill(final Context context, final Locale locale) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Objects.requireNonNull(fallbackSkillInfoList.get(0))
                .build(context, prefs, locale);
    }

    public static List<SkillInfo> getSkillInfoList() {
        return skillInfoList;
    }

    public static List<SkillInfo> getRandomSkillInfoList(final int maxCount) {
        final Random random = new Random();
        if (skillInfoList.size() <= maxCount) {
            final List<SkillInfo> result = new ArrayList<>(skillInfoList);
            Collections.shuffle(result, random);
            return result;

        } else {
            final List<SkillInfo> result = new ArrayList<>();
            while (result.size() < maxCount) {
                final SkillInfo chosen = skillInfoList.get(random.nextInt(skillInfoList.size()));
                if (!result.contains(chosen)) {
                    result.add(chosen);
                }
            }
            return result;
        }
    }


    @DrawableRes
    public static int getSkillIconResource(final SkillInfo skillInfo) {
        @DrawableRes final int skillIconResource = skillInfo.getIconResource();
        return skillIconResource == 0 ? R.drawable.ic_extension_white : skillIconResource;
    }
}
