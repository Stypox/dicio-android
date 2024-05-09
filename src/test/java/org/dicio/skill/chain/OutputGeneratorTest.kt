package org.dicio.skill.chain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.dicio.skill.Skill
import org.dicio.skill.SkillTest
import org.dicio.skill.buildEmptySkill
import java.util.Arrays
import java.util.Collections

class OutputGeneratorTest : StringSpec({
    "next skills" {
        val og = object : OutputGenerator<Void>() {
            override fun generate(data: Void) {}
        }

        val skills = listOf(buildEmptySkill(), buildEmptySkill())
        og.nextSkills().shouldBeEmpty()

        og.setNextSkills(skills)
        og.nextSkills() shouldBeSameInstanceAs skills
        og.nextSkills().shouldBeEmpty()
        og.nextSkills().shouldBeEmpty()

        og.setNextSkills(skills)
        og.nextSkills() shouldBeSameInstanceAs skills
        og.nextSkills().shouldBeEmpty()

        og.setNextSkills(skills)
        og.cleanup()
        og.nextSkills().shouldBeEmpty()
    }
})
