package org.dicio.dicio_android.skills;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.Sections;
import org.dicio.dicio_android.skills.calculator.CalculatorInfo;
import org.dicio.dicio_android.skills.fallback.text.TextFallbackInfo;
import org.dicio.dicio_android.skills.lyrics.LyricsInfo;
import org.dicio.dicio_android.skills.open.OpenInfo;
import org.dicio.dicio_android.skills.search.SearchInfo;
import org.dicio.dicio_android.skills.telephone.TelephoneInfo;
import org.dicio.dicio_android.skills.timer.TimerInfo;
import org.dicio.dicio_android.skills.weather.WeatherInfo;
import org.dicio.numbers.NumberParserFormatter;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SkillHandler {

    // TODO improve id handling (maybe just use an int that can point to an Android resource)
    private static final List<SkillInfo> skillInfoList = new ArrayList<SkillInfo>() {{
        add(new WeatherInfo());
        add(new SearchInfo());
        add(new LyricsInfo());
        add(new OpenInfo());
        add(new CalculatorInfo());
        add(new TelephoneInfo());
        add(new TimerInfo());
    }};

    private static final List<SkillInfo> fallbackSkillInfoList = new ArrayList<SkillInfo>() {{
        add(new TextFallbackInfo());
    }};

    @SuppressLint("StaticFieldLeak") // releaseSkillContext() is called in MainActivity.onDestroy()
    @Nullable private static SkillContext context = null;


    /**
     * Sets up the static skill context used throughout the app. Requires the sections locale to
     * be ready. Has to be called before working with skills or skill infos.
     * @param androidContext the android context to use in the skill context
     */
    public static void setupSkillContext(final Context androidContext) {
        if (Sections.getCurrentLocale() == null) {
            throw new RuntimeException(
                    "setupSkillContext() requires the Sections locale to be initialized");
        }

        @Nullable NumberParserFormatter numberParserFormatter = null;
        try {
            numberParserFormatter = new NumberParserFormatter(Sections.getCurrentLocale());
        } catch (final IllegalArgumentException ignored) {
            // current locale is not supported by dicio-numbers
        }

        context = new SkillContext(androidContext,
                PreferenceManager.getDefaultSharedPreferences(androidContext),
                Sections.getCurrentLocale(),
                numberParserFormatter);
    }

    public static void releaseSkillContext() {
        context = null;
    }

    @Nullable
    public static SkillContext getSkillContext() {
        return context;
    }

    private static void assertSkillContextNotNull() {
        if (context == null) {
            throw new RuntimeException("Skill context is null");
        }
    }


    public static String getIsEnabledPreferenceKey(final String skillId) {
        return "skills_handler_is_enabled_" + skillId;
    }


    public static List<Skill> getStandardSkillBatch() {
        assertSkillContextNotNull();
        final List<Skill> result = new ArrayList<>();

        for (final SkillInfo skillInfo : getEnabledSkillInfoList()) {
            result.add(skillInfo.build(context));
        }

        return result;
    }

    public static Skill getFallbackSkill() {
        assertSkillContextNotNull();
        return Objects.requireNonNull(fallbackSkillInfoList.get(0)).build(context);
    }


    public static List<SkillInfo> getAllSkillInfoList() {
        return skillInfoList;
    }

    public static List<SkillInfo> getAvailableSkillInfoList() {
        assertSkillContextNotNull();
        final List<SkillInfo> result = new ArrayList<>();

        for (final SkillInfo skillInfo : skillInfoList) {
            if (skillInfo.isAvailable(context)) {
                result.add(skillInfo);
            }
        }

        return result;
    }

    public static List<SkillInfo> getEnabledSkillInfoList() {
        assertSkillContextNotNull();
        //noinspection ConstantConditions (context is not null, asserted just above here)
        final SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(context.getAndroidContext());
        final List<SkillInfo> result = new ArrayList<>();

        for (final SkillInfo skillInfo : skillInfoList) {
            // check both if available and enabled
            if (skillInfo.isAvailable(context)
                    && prefs.getBoolean(getIsEnabledPreferenceKey(skillInfo.getId()), true)) {
                result.add(skillInfo);
            }
        }

        return result;
    }

    public static List<SkillInfo> getEnabledSkillInfoListShuffled() {
        final List<SkillInfo> enabledSkillInfoList = getEnabledSkillInfoList();
        Collections.shuffle(enabledSkillInfoList);
        return enabledSkillInfoList;
    }


    @DrawableRes
    public static int getSkillIconResource(final SkillInfo skillInfo) {
        @DrawableRes final int skillIconResource = skillInfo.getIconResource();
        return skillIconResource == 0 ? R.drawable.ic_extension_white : skillIconResource;
    }
}
