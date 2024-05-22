package org.stypox.dicio.skills

import androidx.annotation.DrawableRes
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.skills.calculator.CalculatorInfo
import org.stypox.dicio.skills.current_time.CurrentTimeInfo
import org.stypox.dicio.skills.fallback.text.TextFallbackInfo
import org.stypox.dicio.skills.lyrics.LyricsInfo
import org.stypox.dicio.skills.navigation.NavigationInfo
import org.stypox.dicio.skills.open.OpenInfo
import org.stypox.dicio.skills.search.SearchInfo
import org.stypox.dicio.skills.telephone.TelephoneInfo
import org.stypox.dicio.skills.timer.TimerInfo
import org.stypox.dicio.skills.weather.WeatherInfo
import java.util.Objects
import java.util.stream.Collectors
import javax.inject.Inject

class SkillHandler2 @Inject constructor(
    private val skillContext: SkillContext
) {
    // TODO improve id handling (maybe just use an int that can point to an Android resource)
    val allSkillInfoList = listOf(
        WeatherInfo,
        SearchInfo,
        LyricsInfo,
        OpenInfo,
        CalculatorInfo,
        NavigationInfo,
        TelephoneInfo,
        TimerInfo,
        CurrentTimeInfo,
    )

    private val fallbackSkillInfoList = listOf(TextFallbackInfo)

    fun getIsEnabledPreferenceKey(skillId: String): String {
        return "skills_handler_is_enabled_$skillId"
    }

    val standardSkillBatch: List<Skill<*>>
        get() = enabledSkillInfoList.stream()
            .map(::buildSkillFromInfo)
            .collect(Collectors.toList())
    val fallbackSkill: Skill<*>
        get() = buildSkillFromInfo(Objects.requireNonNull(fallbackSkillInfoList[0]))

    private fun buildSkillFromInfo(skillInfo: SkillInfo): Skill<*> {
        return skillInfo.build(skillContext)
    }

    val availableSkillInfoList: List<SkillInfo>
        get() = allSkillInfoList.stream()
            .filter { skillInfo: SkillInfo -> skillInfo.isAvailable(skillContext) }
            .collect(Collectors.toList())

    val enabledSkillInfoList: List<SkillInfo>
        get() = allSkillInfoList.stream()
            .filter { skillInfo: SkillInfo ->
                skillInfo.isAvailable(skillContext) && skillContext.preferences
                    .getBoolean(getIsEnabledPreferenceKey(skillInfo.id), true)
            }
            .collect(Collectors.toList())

    val enabledSkillInfoListShuffled: List<SkillInfo>
        get() = enabledSkillInfoList.shuffled()

    @DrawableRes
    fun getSkillIconResource(skillInfo: SkillInfo): Int {
        @DrawableRes val skillIconResource = skillInfo.iconResource
        return if (skillIconResource == 0) R.drawable.ic_extension_white else skillIconResource
    }
}
