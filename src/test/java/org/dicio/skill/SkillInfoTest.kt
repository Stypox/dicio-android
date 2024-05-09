package org.dicio.skill

import androidx.fragment.app.Fragment
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class SkillInfoTest : StringSpec({
    "constructor and getters" {
        val skillInfo: SkillInfo = object : SkillInfo("testId", 11, 222, 0, true) {
            override fun isAvailable(context: SkillContext): Boolean {
                return false
            }

            override fun build(context: SkillContext): Skill {
                throw NotImplementedError()
            }

            override val preferenceFragment: Fragment?
                get() = null
        }

        skillInfo.id shouldBeSameInstanceAs "testId"
        skillInfo.nameResource shouldBe 11
        skillInfo.sentenceExampleResource shouldBe 222
        skillInfo.iconResource shouldBe 0
        skillInfo.hasPreferences.shouldBeTrue()
    }

    "get needed permissions" {
        val skillInfo: SkillInfo = object : SkillInfo("", 0, 0, 0, false) {
            override fun isAvailable(context: SkillContext): Boolean {
                return false
            }

            override fun build(context: SkillContext): Skill {
                throw NotImplementedError()
            }

            override val preferenceFragment: Fragment?
                get() = null
        }

        withClue("Needed permissions should be empty by default") {
            skillInfo.neededPermissions.shouldBeEmpty()
        }
    }
})
