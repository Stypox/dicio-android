package org.stypox.dicio.skills;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.skills.navigate.NavigateInfo;
import org.stypox.dicio.Sections;
import org.stypox.dicio.skills.calculator.CalculatorInfo;
import org.stypox.dicio.skills.fallback.text.TextFallbackInfo;
import org.stypox.dicio.skills.lyrics.LyricsInfo;
import org.stypox.dicio.skills.open.OpenInfo;
import org.stypox.dicio.skills.search.SearchInfo;
import org.stypox.dicio.skills.telephone.TelephoneInfo;
import org.stypox.dicio.skills.timer.TimerInfo;
import org.stypox.dicio.skills.weather.WeatherInfo;
import org.stypox.dicio.skills.current_time.CurrentTimeInfo;
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
import java.util.stream.Collectors;

public final class SkillHandler {

    private SkillHandler() {
    }

    // TODO improve id handling (maybe just use an int that can point to an Android resource)
    private static final List<SkillInfo> SKILL_INFO_LIST = new ArrayList<>() {{
        add(new WeatherInfo());
        add(new SearchInfo());
        add(new LyricsInfo());
        add(new OpenInfo());
        add(new CalculatorInfo());
        add(new NavigateInfo());
        add(new TelephoneInfo());
        add(new TimerInfo());
        add(new CurrentTimeInfo());
    }};

    private static final List<SkillInfo> FALLBACK_SKILL_INFO_LIST = new ArrayList<>() {{
        add(new TextFallbackInfo());
    }};

    @SuppressLint("StaticFieldLeak") // releaseSkillContext() is called in MainActivity.onDestroy()
    private static final SkillContext CONTEXT = new SkillContext();


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

        CONTEXT.setAndroidContext(androidContext);
        CONTEXT.setPreferences(PreferenceManager.getDefaultSharedPreferences(androidContext));
        CONTEXT.setLocale(Sections.getCurrentLocale());
        CONTEXT.setNumberParserFormatter(numberParserFormatter);
    }

    /**
     * Sets the provided devices in the static skill context used throughout the app. Has to be
     * called before requesting any skill output.
     * @param speechOutputDevice the speech output device to use in the skill context
     * @param graphicalOutputDevice the graphical output device to use in the skill context
     */
    public static void setSkillContextDevices(final SpeechOutputDevice speechOutputDevice,
                                              final GraphicalOutputDevice graphicalOutputDevice) {
        CONTEXT.setSpeechOutputDevice(speechOutputDevice);
        CONTEXT.setGraphicalOutputDevice(graphicalOutputDevice);
    }


    @SuppressWarnings("ConstantConditions") // we want to release resources, so we set to null
    public static void releaseSkillContext() {
        CONTEXT.setAndroidContext(null);
        CONTEXT.setPreferences(null);
    }

    public static SkillContext getSkillContext() {
        return CONTEXT;
    }


    public static String getIsEnabledPreferenceKey(final String skillId) {
        return "skills_handler_is_enabled_" + skillId;
    }


    public static List<Skill> getStandardSkillBatch() {
        return getEnabledSkillInfoList().stream()
                .map(SkillHandler::buildSkillFromInfo)
                .collect(Collectors.toList());
    }

    public static Skill getFallbackSkill() {
        return buildSkillFromInfo(Objects.requireNonNull(FALLBACK_SKILL_INFO_LIST.get(0)));
    }

    private static Skill buildSkillFromInfo(@NonNull final SkillInfo skillInfo) {
        final Skill skill = skillInfo.build(CONTEXT);
        skill.setContext(CONTEXT);
        skill.setSkillInfo(skillInfo);
        return skill;
    }


    public static List<SkillInfo> getAllSkillInfoList() {
        return SKILL_INFO_LIST;
    }

    public static List<SkillInfo> getAvailableSkillInfoList() {
        return SKILL_INFO_LIST.stream()
                .filter(skillInfo -> skillInfo.isAvailable(CONTEXT))
                .collect(Collectors.toList());
    }

    public static List<SkillInfo> getEnabledSkillInfoList() {
        return SKILL_INFO_LIST.stream()
                .filter(skillInfo -> skillInfo.isAvailable(CONTEXT) && CONTEXT.getPreferences()
                        .getBoolean(getIsEnabledPreferenceKey(skillInfo.getId()), true))
                .collect(Collectors.toList());
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
