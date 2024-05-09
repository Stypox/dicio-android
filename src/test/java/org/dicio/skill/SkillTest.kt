package org.dicio.skill

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.dicio.skill.chain.InputRecognizer.Specificity

class SkillTest : StringSpec({
    "next skills should initially be empty" {
        val skill = buildEmptySkill()
        skill.nextSkills().shouldBeEmpty()
    }

    "getting next skills should return the last value set with the setter" {
        val skill = buildEmptySkill()
        skill.setNextSkills(listOf(skill))
        skill.nextSkills().shouldHaveSize(1)
    }

    "getting next skills should clear them" {
        val skill = buildEmptySkill()
        skill.setNextSkills(listOf(skill))
        skill.nextSkills().shouldHaveSize(1)
        skill.nextSkills().shouldBeEmpty()
    }
})

fun buildEmptySkill(): Skill {
    return object : Skill() {
        override fun specificity(): Specificity {
            return Specificity.MEDIUM
        }

        override fun setInput(
            input: String, inputWords: List<String>,
            normalizedWordKeys: List<String>
        ) {
        }

        override fun score(): Float {
            return 0.0f
        }

        override fun processInput() {}
        override fun generateOutput() {}
        override fun cleanup() {}
    }
}
