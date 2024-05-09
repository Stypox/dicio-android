package org.dicio.skill

import org.dicio.skill.chain.InputRecognizer.Specificity
import org.junit.Assert
import org.junit.Test

class FallbackSkillTest {
    @Test
    fun testConstructor() {
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

    @Test
    fun testScoreAndSpecificity() {
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

        Assert.assertEquals(0.0f, skill.score(), 0.0f)
        Assert.assertSame(Specificity.LOW, skill.specificity())
    }
}
