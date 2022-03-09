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
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

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
    private static final SkillContext context = new SkillContext();


    /**
     * Sets the provided Android context, the preferences obtained from it, the current sections
     * locale and the number parser formatter in the static skill context used throughout the app.
     * Requires the sections locale to be ready. Has to be called before working with skills or
     * skill infos.
     * @param androidContext the android context to use in the skill context
     */
    public static void setSkillContextAndroidAndLocale(final Context androidContext) {
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

        context.setAndroidContext(androidContext);
        context.setPreferences(PreferenceManager.getDefaultSharedPreferences(androidContext));
        context.setLocale(Sections.getCurrentLocale());
        context.setNumberParserFormatter(numberParserFormatter);
    }

    /**
     * Sets the provided devices in the static skill context used throughout the app. Has to be
     * called before requesting any skill output.
     * @param speechOutputDevice the speech output device to use in the skill context
     * @param graphicalOutputDevice the graphical output device to use in the skill context
     */
    public static void setSkillContextDevices(final SpeechOutputDevice speechOutputDevice,
                                              final GraphicalOutputDevice graphicalOutputDevice) {
        context.setSpeechOutputDevice(speechOutputDevice);
        context.setGraphicalOutputDevice(graphicalOutputDevice);
    }


    @SuppressWarnings("ConstantConditions") // we want to release resources, so we set to null
    public static void releaseSkillContext() {
        context.setAndroidContext(null);
        context.setPreferences(null);
    }

    public static SkillContext getSkillContext() {
        return context;
    }


    public static String getIsEnabledPreferenceKey(final String skillId) {
        return "skills_handler_is_enabled_" + skillId;
    }


    public static List<Skill> getStandardSkillBatch() {
        final List<Skill> result = new ArrayList<>();

        for (final SkillInfo skillInfo : getEnabledSkillInfoList()) {
            final Skill skill = skillInfo.build(context);
            skill.setContext(context);
            skill.setSkillInfo(skillInfo);
            result.add(skill);
        }

        return result;
    }

    public static Skill getFallbackSkill() {
        return Objects.requireNonNull(fallbackSkillInfoList.get(0)).build(context);
    }


    public static List<SkillInfo> getAllSkillInfoList() {
        return skillInfoList;
    }

    public static List<SkillInfo> getAvailableSkillInfoList() {
        final List<SkillInfo> result = new ArrayList<>();

        for (final SkillInfo skillInfo : skillInfoList) {
            if (skillInfo.isAvailable(context)) {
                result.add(skillInfo);
            }
        }

        return result;
    }

    public static List<SkillInfo> getEnabledSkillInfoList() {
        final SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(context.android());
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
