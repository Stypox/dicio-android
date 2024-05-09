package org.dicio.skill

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.dicio.skill.chain.InputRecognizer.Specificity

class FallbackSkillTest : StringSpec({
    "constructor" {
        object : FallbackSkill() {
            override fun setInput(
                input: String, inputWords: List<String>,
                normalizedWordKeys: List<String>
            ) {
            }

            override fun processInput() {}
            override fun generateOutput() {}
            override fun cleanup() {}
        }
    }

    "score and specificity" {
        val skill: Skill = object : FallbackSkill() {
            override fun setInput(
                input: String, inputWords: List<String>,
                normalizedWordKeys: List<String>
            ) {
            }

            override fun processInput() {}
            override fun generateOutput() {}
            override fun cleanup() {}
        }

        skill.score() shouldBe 0.0f
        skill.specificity() shouldBeSameInstanceAs Specificity.LOW
    }
})
