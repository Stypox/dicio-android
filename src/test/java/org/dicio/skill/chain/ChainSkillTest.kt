package org.dicio.skill.chain

import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.dicio.skill.Skill
import org.dicio.skill.SkillComponent
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.output.SkillOutput

class ChainSkillTest : StringSpec({
    "build, no intermediate processor" {
        val skill: Skill = ChainSkill.Builder(TestSkillInfo, ir)
            .output(og)

        skill.specificity shouldBeSameInstanceAs ir.specificity
        assertGeneratedOutput(java.lang.Integer::class.java.toString() + "|" + 8, skill)
    }

    "build, one intermediate processor" {
        val skill: Skill = ChainSkill.Builder(TestSkillInfo, ir)
            .process(ip1)
            .output(og)

        skill.specificity shouldBeSameInstanceAs ir.specificity
        assertGeneratedOutput(java.lang.Double::class.java.toString() + "|" + (8 / 3.0), skill)
    }

    "build, two intermediate processors" {
        val skill: Skill = ChainSkill.Builder(TestSkillInfo, ir)
            .process(ip1)
            .process(ip2)
            .output(og)

        skill.specificity shouldBeSameInstanceAs ir.specificity
        assertGeneratedOutput(java.lang.Float::class.java.toString() + "|" + (2 * (8 / 3.0)).toFloat(), skill)
    }
})

object TestSkillInfo : SkillInfo("", 0, 0, 0, false) {
    override fun isAvailable(context: SkillContext) = TODO()
    override fun build(context: SkillContext) = TODO()
    override val preferenceFragment get() = TODO()
}

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
private val ir = object : InputRecognizer<Int>(Specificity.HIGH) {
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

private val og = object : OutputGenerator<Any>() {
    override fun generate(data: Any): SkillOutput {
        data.shouldNotBeNull()
        return object : SkillOutput {
            override fun getSpeechOutput(ctx: SkillContext): String = "${data.javaClass}|$data"
            @Composable override fun GraphicalOutput(ctx: SkillContext) {}
        }
    }

    override fun cleanup() {}
}

@Throws(Exception::class)
private fun assertGeneratedOutput(expected: String, skill: Skill) {
    skill.processInput()
    skill.generateOutput().getSpeechOutput(SkillContext()) shouldBe expected
}
