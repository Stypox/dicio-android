package org.dicio.skill.standard.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.dicio.skill.standard.construct.beEqualToPlusOrMinus
import org.dicio.skill.standard.construct.s

class MemToEndTest : DescribeSpec({
    describe("initialMemToEnd") {
        it("empty input") {
            initialMemToEnd(floatArrayOf(0f)) shouldBe arrayOf(s(0f,0f,0f,0f))
        }
        it("empty input but broken cumulativeWeight") {
            initialMemToEnd(floatArrayOf(214.23f)) shouldBe arrayOf(s(0f,0f,0f,0f))
        }
        it("random cumulativeWeight") {
            initialMemToEnd(floatArrayOf(0f,2f,5f,9f,14f)) shouldBe arrayOf(
                s(0f,14f,0f,0f),s(0f,12f,0f,0f),s(0f,9f,0f,0f),s(0f,5f,0f,0f),s(0f,0f,0f,0f)
            )
        }
        it("random input") {
            initialMemToEnd(cumulativeWeight("a.4b")) should beEqualToPlusOrMinus(
                s(0f,2.15f,0f,0f),s(0f,1.15f,0f,0f),s(0f,1.1f,0f,0f),s(0f,1f,0f,0f),s(0f,0f,0f,0f)
            )
        }
    }

    describe("normalizeMemToEnd") {
        it("empty input") {
            val memToEnd = arrayOf(s(0f,0f,0f,12f))
            normalizeMemToEnd(memToEnd, floatArrayOf(0f))
            memToEnd shouldBe arrayOf(s(0f,0f,0f,12f))
        }
        it("empty input but broken cumulativeWeight") {
            val memToEnd = arrayOf(s(0f,0f,0f,12f))
            normalizeMemToEnd(memToEnd, floatArrayOf(214.23f))
            memToEnd shouldBe arrayOf(s(0f,0f,0f,12f))
        }
        it("random input") {
            val cumulativeWeight = cumulativeWeight("c b 2? b d")

            // simulate having just matched word "b", but not having yet called `normalizeMemToEnd`
            val memToEnd = initialMemToEnd(cumulativeWeight)
                .map { it.plus(refWeight = 1f) }
                .toTypedArray()
            memToEnd[2] = memToEnd[2].plus(userMatched = 1.0f, refMatched = 1.0f)
            memToEnd[7] = memToEnd[7].plus(userMatched = 1.0f, refMatched = 1.0f)

            normalizeMemToEnd(memToEnd, cumulativeWeight)
            memToEnd should beEqualToPlusOrMinus(
                s(1f,4.15f,1f,1f),s(1f,3.15f,1f,1f),s(1f,3.15f,1f,1f),s(1f,2.15f,1f,1f),
                s(1f,2.15f,1f,1f),s(1f,2.05f,1f,1f),s(1f,2f,1f,1f),s(1f,2f,1f,1f),
                s(0f,1f,0f,1f),s(0f,1f,0f,1f),s(0f,0f,0f,1f),
            )
        }
    }
})
