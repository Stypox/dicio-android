package org.dicio.skill

import android.content.Context
import android.content.SharedPreferences
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.dicio.numbers.ParserFormatter
import org.dicio.skill.output.SpeechOutputDevice
import java.util.Locale

class SkillComponentTest : StringSpec({
    "constructor, getters and setters" {
        val skill: SkillComponent = object : SkillComponent() {}

        skill.setContext(TestSkillContext)
        skill.ctx() shouldBeSameInstanceAs TestSkillContext
    }
})

object TestSkillContext : SkillContext {
    override val android: Context get() = TODO()
    override val preferences: SharedPreferences get() = TODO()
    override val locale: Locale get() = TODO()
    override val parserFormatter: ParserFormatter get() = TODO()
    override val speechOutputDevice: SpeechOutputDevice get() = TODO()
}
