package org.dicio.skill.chain

import org.dicio.skill.Skill
import org.dicio.skill.SkillTest
import org.junit.Assert
import org.junit.Test
import java.util.Arrays
import java.util.Collections

class OutputGeneratorTest {
    @Test
    fun testNextSkills() {
        val og = object : OutputGenerator<Void>() {
            override fun generate(data: Void) {}
        }

        val skills = listOf(SkillTest.buildEmptySkill(), SkillTest.buildEmptySkill())
        Assert.assertTrue(og.nextSkills().isEmpty())

        og.setNextSkills(skills)
        Assert.assertSame(skills, og.nextSkills())
        Assert.assertTrue(og.nextSkills().isEmpty())
        Assert.assertTrue(og.nextSkills().isEmpty())

        og.setNextSkills(skills)
        Assert.assertSame(skills, og.nextSkills())
        Assert.assertTrue(og.nextSkills().isEmpty())

        og.setNextSkills(skills)
        og.cleanup()
        Assert.assertTrue(og.nextSkills().isEmpty())
    }
}
