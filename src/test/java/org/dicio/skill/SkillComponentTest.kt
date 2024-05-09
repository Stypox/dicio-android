package org.dicio.skill

import androidx.fragment.app.Fragment
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeSameInstanceAs

class SkillComponentTest : StringSpec({
    "constructor, getters and setters" {
        val skillInfo: SkillInfo = object : SkillInfo("id", 0, 0, 0, false) {
            override fun isAvailable(context: SkillContext): Boolean {
                return false
            }

            override fun build(context: SkillContext): Skill {
                throw NotImplementedError()
            }

            override val preferenceFragment: Fragment?
                get() = null
        }

        val skill: SkillComponent = object : SkillComponent() {}

        skill.skillInfo.shouldBeNull()
        skill.skillInfo = skillInfo
        skill.skillInfo shouldBeSameInstanceAs skillInfo

        val skillContext = SkillContext()
        skill.setContext(skillContext)
        skill.ctx() shouldBeSameInstanceAs skillContext
    }
})
