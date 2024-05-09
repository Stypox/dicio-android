package org.dicio.skill

import org.dicio.skill.chain.InputRecognizer.Specificity
import org.junit.Assert
import org.junit.Test

class SkillTest {
    @Test
    fun testConstructor() {
        val skill = buildEmptySkill()

        Assert.assertTrue(skill.nextSkills().isEmpty())
    }

    companion object {
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
    }
}
