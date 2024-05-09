package org.dicio.skill.chain

import androidx.fragment.app.Fragment
import org.dicio.skill.Skill
import org.dicio.skill.SkillComponent
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.junit.Assert
import org.junit.Test

class ChainSkillTest {
    @Test
    @Throws(Exception::class)
    fun testBuildNoIntermediateProcessors() {
        val skill: Skill = ChainSkill.Builder(ir)
            .output(og)

        assertSetSkillInfo(skill, ir, og)
        Assert.assertEquals(ir.specificity(), skill.specificity())
        assertGeneratedOutput(java.lang.Integer::class.java.toString() + "|" + 8, skill)
    }

    @Test
    @Throws(Exception::class)
    fun testBuildOneIntermediateProcessor() {
        val skill: Skill = ChainSkill.Builder(ir)
            .process(ip1)
            .output(og)

        assertSetSkillInfo(skill, ir, ip1, og)
        Assert.assertEquals(ir.specificity(), skill.specificity())
        assertGeneratedOutput(java.lang.Double::class.java.toString() + "|" + (8 / 3.0), skill)
    }

    @Test
    @Throws(Exception::class)
    fun testBuildTwoIntermediateProcessors() {
        val skill: Skill = ChainSkill.Builder(ir)
            .process(ip1)
            .process(ip2)
            .output(og)

        assertSetSkillInfo(skill, ir, ip1, ip2, og)
        Assert.assertEquals(ir.specificity(), skill.specificity())
        assertGeneratedOutput(java.lang.Float::class.java.toString() + "|" + (2 * (8 / 3.0)).toFloat(), skill)
    }

    companion object {
        private val skillInfo: SkillInfo = object : SkillInfo("", 0, 0, 0, false) {
            override fun isAvailable(context: SkillContext): Boolean {
                return false
            }

            override fun build(context: SkillContext): Skill {
                throw NotImplementedError()
            }

            override val preferenceFragment: Fragment?
                get() = null
        }

        // the type parameters used here are just random: the chain skill does not check if they match
        private val ir = object : InputRecognizer<Int>() {
            override fun specificity(): Specificity {
                return Specificity.HIGH
            }

            override fun setInput(
                input: String, inputWords: List<String>,
                normalizedInputWords: List<String>
            ) {
            }

            override fun score(): Float {
                return 0.0f
            }

            override val result: Int
                get() = 8

            override fun cleanup() {}
        }

        private val ip1 = object : IntermediateProcessor<Int, Double>() {
            override fun process(data: Int): Double {
                return data / 3.0
            }
        }

        private val ip2 = object : IntermediateProcessor<Double, Float>() {
            override fun process(data: Double): Float {
                return (2 * data).toFloat()
            }
        }

        private val generatedOutput = StringBuilder()
        private val og = object : OutputGenerator<Any>() {
            override fun generate(data: Any) {
                Assert.assertNotNull(data)
                generatedOutput.append(data.javaClass).append("|").append(data)
            }

            override fun cleanup() {}
        }

        @Throws(Exception::class)
        private fun assertGeneratedOutput(expected: String, skill: Skill) {
            // use static generatedOutput variable
            generatedOutput.setLength(0)
            skill.processInput()
            skill.generateOutput()
            Assert.assertEquals(expected, generatedOutput.toString())
        }

        private fun assertSetSkillInfo(skill: Skill, vararg skillComponents: SkillComponent) {
            skill.skillInfo = null
            for (skillComponent in skillComponents) {
                skillComponent.skillInfo = null
            }

            skill.skillInfo = skillInfo

            Assert.assertSame(skillInfo, skill.skillInfo)
            for (skillComponent in skillComponents) {
                Assert.assertSame(skillInfo, skillComponent.skillInfo)
            }
        }
    }
}
