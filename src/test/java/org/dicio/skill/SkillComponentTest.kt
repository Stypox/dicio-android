package org.dicio.skill

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs

class SkillComponentTest : StringSpec({
    "constructor, getters and setters" {
        val skill: SkillComponent = object : SkillComponent() {}

        val skillContext = SkillContext()
        skill.setContext(skillContext)
        skill.ctx() shouldBeSameInstanceAs skillContext
    }
})
