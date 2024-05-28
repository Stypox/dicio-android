package org.dicio.skill

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
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
    override val locale: Locale get() = mocked()
    override val parserFormatter: ParserFormatter get() = mocked()
    override val speechOutputDevice: SpeechOutputDevice get() = mocked()
}

object MockSkillInfo : SkillInfo("") {
    override fun name(context: Context): String = mocked()
    override fun sentenceExample(context: Context): String = mocked()
    @Composable override fun icon(): Painter = mocked()
    override fun isAvailable(ctx: SkillContext) = mocked()
    override fun build(ctx: SkillContext) = mocked()
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
