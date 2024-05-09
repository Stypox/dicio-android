package org.stypox.dicio.eval

import org.dicio.skill.Skill
import org.dicio.skill.chain.InputRecognizer
import org.dicio.skill.util.WordExtractor.extractWords
import org.dicio.skill.util.WordExtractor.normalizeWords
import org.junit.Assert
import org.junit.Test

class SkillRankerTest {
    private class TestSkill(val specificity: InputRecognizer.Specificity, val score: Float) :
        Skill() {
        var input: String? = null

        override fun specificity(): InputRecognizer.Specificity {
            return specificity
        }

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
        override fun generateOutput() {}
        override fun cleanup() {}
    }


    @Test
    fun testOrderPreservedAndCorrectInput() {
        val ac1 = TestSkill(InputRecognizer.Specificity.HIGH, 0.80f)
        val ac2 = TestSkill(InputRecognizer.Specificity.HIGH, 0.93f)
        val ac3 = TestSkill(InputRecognizer.Specificity.HIGH, 1.00f)
        val acMed = TestSkill(InputRecognizer.Specificity.MEDIUM, 1.00f)
        val acLow = TestSkill(InputRecognizer.Specificity.LOW, 1.00f)

        val cr =
            getRanker(TestSkill(InputRecognizer.Specificity.LOW, 0.0f), ac1, acMed, ac2, acLow, ac3)
        cr.getBest(INPUT, INPUT_WORDS, NORMALIZED_WORD_KEYS)

        Assert.assertEquals(INPUT, ac1.input)
        Assert.assertEquals(INPUT, ac2.input)
        Assert.assertNull(ac3.input)
        Assert.assertNull(acMed.input)
        Assert.assertNull(acLow.input)
    }

    @Test
    fun testHighPrHighScore() {
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


    @Test
    fun testHighPrLowScore() {
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

    @Test
    fun testNoMatch() {
        val fallback = TestSkill(InputRecognizer.Specificity.LOW, 0.0f)
        val cr = getRanker(fallback, TestSkill(InputRecognizer.Specificity.LOW, 0.8f))
        val result = cr.getBest(INPUT, INPUT_WORDS, NORMALIZED_WORD_KEYS) as TestSkill?
        Assert.assertNull(result) // make sure the fallback is not returned (this was once the case)
    }

    @Test
    fun testGetFallbackSkill() {
        val fallback = TestSkill(InputRecognizer.Specificity.LOW, 0.0f)
        val cr = getRanker(fallback, TestSkill(InputRecognizer.Specificity.LOW, 0.8f))
        val gotFallback = cr.getFallbackSkill(INPUT, INPUT_WORDS, NORMALIZED_WORD_KEYS)
        Assert.assertSame(fallback, gotFallback)
        Assert.assertEquals(INPUT, (gotFallback as TestSkill).input)
    }

    companion object {
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
            val result =
                cr.getBest("", emptyList(), emptyList())
            val message = if (result === fallback) {
                "Fallback skill returned by getBest"
            } else {
                ("Skill with specificity " + result!!.specificity()
                        + " and score " + result.score() + " returned by getBest")
            }

            Assert.assertSame(message, best, result)
        }
    }
}
