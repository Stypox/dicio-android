package org.stypox.dicio.eval

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.chain.InputRecognizer
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.util.WordExtractor.extractWords
import org.dicio.skill.util.WordExtractor.normalizeWords

object TestSkillInfo : SkillInfo("", 0, 0, 0, false) {
    override fun isAvailable(context: SkillContext) = TODO()
    override fun build(context: SkillContext) = TODO()
    override val preferenceFragment get() = TODO()
}

private class TestSkill(specificity: InputRecognizer.Specificity, val score: Float) :
    Skill(TestSkillInfo, specificity) {
    var input: String? = null

    override fun score(): Float {
        return score
    }


    override fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ) {
        this.input = input
    }

    // useless for this test
    override fun processInput() {}
    override fun generateOutput(): SkillOutput = TODO()
    override fun cleanup() {}
}


const val INPUT: String = "hi"
val INPUT_WORDS: List<String> = extractWords(INPUT)
val NORMALIZED_WORD_KEYS: List<String> = normalizeWords(INPUT_WORDS)

private fun getRanker(
    fallback: Skill,
    vararg others: Skill
): SkillRanker {
    return SkillRanker(listOf(*others), fallback)
}

private fun assertRanked(
    cr: SkillRanker,
    fallback: TestSkill,
    best: TestSkill
) {
    val result = cr.getBest("", listOf(), listOf())
    result shouldNotBe null

    withClue(if (result === fallback) {
        "Fallback skill returned by getBest"
    } else {
        "Skill with specificity ${result!!.specificity} and score ${
            result.score()} returned by getBest"
    }) {
        result shouldBeSameInstanceAs best
    }
}

class SkillRankerTest : StringSpec({
    "order is preserved and input is correct" {
        val ac1 = TestSkill(InputRecognizer.Specificity.HIGH, 0.80f)
        val ac2 = TestSkill(InputRecognizer.Specificity.HIGH, 0.93f)
        val ac3 = TestSkill(InputRecognizer.Specificity.HIGH, 1.00f)
        val acMed = TestSkill(InputRecognizer.Specificity.MEDIUM, 1.00f)
        val acLow = TestSkill(InputRecognizer.Specificity.LOW, 1.00f)

        val cr =
            getRanker(TestSkill(InputRecognizer.Specificity.LOW, 0.0f), ac1, acMed, ac2, acLow, ac3)
        cr.getBest(INPUT, INPUT_WORDS, NORMALIZED_WORD_KEYS)

        ac1.input shouldBe INPUT
        ac2.input shouldBe INPUT
        ac3.input shouldBe null
        acMed.input shouldBe null
        acLow.input shouldBe null
    }

    "chosen skill has high specificity and high score, although not the highest, but is evaluated before the highest" {
        val fallback = TestSkill(InputRecognizer.Specificity.LOW, 0.0f)
        val best = TestSkill(InputRecognizer.Specificity.HIGH, 0.92f)
        val cr = getRanker(
            fallback,
            TestSkill(InputRecognizer.Specificity.MEDIUM, 0.95f),
            TestSkill(InputRecognizer.Specificity.HIGH, 0.71f),
            best,
            TestSkill(InputRecognizer.Specificity.HIGH, 1.00f),
            TestSkill(InputRecognizer.Specificity.LOW, 1.0f)
        )
        assertRanked(cr, fallback, best)
    }

    "chosen skill has high score but a low specificity, and other skills with lower score but higher specificity are not chosen" {
        val fallback = TestSkill(InputRecognizer.Specificity.LOW, 0.0f)
        val best = TestSkill(InputRecognizer.Specificity.LOW, 1.0f)
        val cr = getRanker(
            fallback,
            TestSkill(InputRecognizer.Specificity.MEDIUM, 0.81f),
            TestSkill(InputRecognizer.Specificity.HIGH, 0.71f),
            TestSkill(InputRecognizer.Specificity.LOW, 0.85f),
            best,
            TestSkill(InputRecognizer.Specificity.HIGH, 0.32f)
        )
        assertRanked(cr, fallback, best)
    }

    "getBest should return null if there is no match even with the fallback" {
        val fallback = TestSkill(InputRecognizer.Specificity.LOW, 0.0f)
        val cr = getRanker(fallback, TestSkill(InputRecognizer.Specificity.LOW, 0.8f))
        val result = cr.getBest(INPUT, INPUT_WORDS, NORMALIZED_WORD_KEYS)
        result shouldBe null // make sure the fallback is not returned (this was once the case)
    }

    "getFallbackSkill should return the fallback skill" {
        val fallback = TestSkill(InputRecognizer.Specificity.LOW, 0.0f)
        val cr = getRanker(fallback, TestSkill(InputRecognizer.Specificity.LOW, 0.8f))
        val gotFallback = cr.getFallbackSkill(INPUT, INPUT_WORDS, NORMALIZED_WORD_KEYS)
        gotFallback shouldBeSameInstanceAs fallback
        (gotFallback as TestSkill).input shouldBe INPUT
    }
})
