package org.dicio.skill

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.dicio.skill.chain.InputRecognizer.Specificity

class FallbackSkillTest : StringSpec({
    "constructor" {
        object : FallbackSkill(TestSkillInfo) {
            override fun setInput(
                input: String, inputWords: List<String>,
                normalizedWordKeys: List<String>
            ) {
            }

            override fun processInput() {}
            override fun generateOutput() = throw NotImplementedError()
            override fun cleanup() {}
        }
    }

    "score and specificity" {
        val skill: Skill = object : FallbackSkill(TestSkillInfo) {
            override fun setInput(
                input: String, inputWords: List<String>,
                normalizedWordKeys: List<String>
            ) {
            }

            override fun processInput() {}
            override fun generateOutput() = throw NotImplementedError()
            override fun cleanup() {}
        }

        skill.score() shouldBe 0.0f
        skill.specificity shouldBeSameInstanceAs Specificity.LOW
    }
})

object TestSkillInfo : SkillInfo("", 0, 0, 0, false) {
    override fun isAvailable(context: SkillContext) = TODO()
    override fun build(context: SkillContext) = TODO()
    override val preferenceFragment get() = TODO()
}
