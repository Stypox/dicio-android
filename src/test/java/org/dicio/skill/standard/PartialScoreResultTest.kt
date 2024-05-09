package org.dicio.skill.standard

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeGreaterThan
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.dicio.skill.standard.PartialScoreResult.Companion.dropAt0point6
import org.dicio.skill.standard.PartialScoreResult.Companion.dropAt0point75


private const val FLOAT_EQUALS_DELTA = 0.0001f

class PartialScoreResultTest : StringSpec({
    "drop at 0.75" {
        dropAt0point75(0.0f) shouldBe 0.0f.plusOrMinus(FLOAT_EQUALS_DELTA)
        dropAt0point75(1.0f) shouldBe 1.0f.plusOrMinus(FLOAT_EQUALS_DELTA)

        dropAt0point75(0.8f) shouldBeGreaterThan 0.8f
        dropAt0point75(0.7f) shouldBeLessThan 0.65f
        dropAt0point75(0.6f) shouldBeLessThan 0.4f
    }

    "drop at 0.6" {
        dropAt0point6(0.0f) shouldBe 0.0f.plusOrMinus(FLOAT_EQUALS_DELTA)
        dropAt0point6(1.0f) shouldBe 1.0f.plusOrMinus(FLOAT_EQUALS_DELTA)

        dropAt0point6(0.7f) shouldBeGreaterThan 0.8f
        dropAt0point6(0.6f) shouldBeLessThan 0.7f
        dropAt0point6(0.5f) shouldBeLessThan 0.4f
    }

    "score" {
        PartialScoreResult(1000000, 1000000).value(5) shouldBe 0.0f.plusOrMinus(FLOAT_EQUALS_DELTA)
        PartialScoreResult(0, 0).value(5) shouldBe 1.0f.plusOrMinus(FLOAT_EQUALS_DELTA)
    }
})
