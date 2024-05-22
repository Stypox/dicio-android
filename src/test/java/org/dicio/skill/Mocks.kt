package org.dicio.skill

import android.content.Context
import android.content.SharedPreferences
import org.dicio.numbers.ParserFormatter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.context.SpeechOutputDevice
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.dicio.skill.standard.StandardResult
import java.util.Locale

object MockSkillContext : SkillContext {
    override val android: Context get() = mocked()
    override val preferences: SharedPreferences get() = mocked()
    override val locale: Locale get() = mocked()
    override val parserFormatter: ParserFormatter get() = mocked()
    override val speechOutputDevice: SpeechOutputDevice get() = mocked()
}

object MockSkillInfo : SkillInfo("", 0, 0, 0, false) {
    override fun isAvailable(context: SkillContext) = mocked()
    override fun build(context: SkillContext) = mocked()
    override val preferenceFragment get() = mocked()
}

fun mockStandardRecognizerSkill(data: StandardRecognizerData) = object : StandardRecognizerSkill(
    MockSkillInfo,
    data
) {
    override suspend fun generateOutput(
        ctx: SkillContext,
        scoreResult: StandardResult
    ): SkillOutput = mocked()
}

fun mocked(): Nothing {
    throw NotImplementedError()
}
