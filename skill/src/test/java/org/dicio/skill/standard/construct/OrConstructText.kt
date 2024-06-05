package org.dicio.skill.standard.construct

import io.kotest.core.spec.style.DescribeSpec

@Suppress("BooleanLiteralArgument")
class OrConstructText : DescribeSpec({
    describe("empty or non-matching input") {
        it("empty input, with optional construct") {
            OrConstruct(listOf(
                WordConstruct("a", false, false, 1.0f),
                OptionalConstruct(),
            ))
                .withInput("")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0.0f)
        }
        it("empty input, without optional construct") {
            OrConstruct(listOf(
                WordConstruct("a", false, false, 1.0f),
            ))
                .withInput("")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 1.0f)
        }
        it("empty input, should minimize refWeight") {
            OrConstruct(listOf(
                WordConstruct("a", false, false, 1.8f),
                WordConstruct("b", false, false, 0.5f),
                WordConstruct("c", false, false, 1.3f),
                WordConstruct("d", false, false, 1.0f),
            ))
                .withInput("")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0.5f)
        }
        it("empty input, no constructs") {
            OrConstruct(listOf())
                .withInput("")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0.0f)
        }
        it("no constructs") {
            OrConstruct(listOf())
                .withInput(" a b ")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0.0f)
        }
        it("non-matching word OR optional construct") {
            OrConstruct(listOf(
                WordConstruct("a", false, false, 1.0f),
                OptionalConstruct(),
            ))
                .withInput("b")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0.0f)
        }
    }

    describe("something matches") {
        it("both constructs match") {
            val w1 = 1.0f
            val w2 = 0.4f
            val n2 = "name"
            OrConstruct(listOf(
                WordConstruct("a", false, false, 1.0f),
                CapturingConstruct(n2, 0.4f),
            ))
                .withInput("a")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,1f,w1,w1),s(0f,0f,0f,w2))
        }
        it("both constructs match, but in different places") {
            val w1 = 1.0f
            val w2 = 0.4f
            val n2 = "name"
            OrConstruct(listOf(
                WordConstruct("a", false, false, 1.0f),
                CapturingConstruct(n2, 0.4f),
            ))
                .withInput("a.")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,1.05f,w1,w1),s(0.05f,0.05f,w2,w2,capt(n2,1,2)),s(0f,0f,0f,w2))
        }
        it("optional construct should not be chosen, once something starts matching") {
            val w = 1.0f
            OrConstruct(listOf(
                WordConstruct("b", false, false, w),
                OptionalConstruct(),
            ))
                .withInput("a@b.c")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,3.1f,w,w),s(1f,2.1f,w,w),s(1f,2.05f,w,w),s(0f,1.05f,0f,0f),s(0f,1f,0f,0f),s(0f,0f,0f,0f))
        }
        it("more weight should be preferred, if it matches") {
            val w1 = 0.8f
            val w2 = 1.0f
            val w3 = 0.5f
            val w4 = 0.1f // will only be chosen at the end, when nothing matches
            OrConstruct(listOf(
                WordConstruct("a", false, false, w1),
                WordConstruct("b", false, false, w2),
                WordConstruct("c", false, false, w3),
                WordConstruct("nevermatches", false, false, w4),
            ))
                .withInput("a@b.c ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,3.1f,w2,w2),s(1f,2.1f,w2,w2),s(1f,2.05f,w2,w2),s(1f,1.05f,w3,w3),s(1f,1f,w3,w3),s(0f,0f,0f,w4),s(0f,0f,0f,w4))
        }
    }
})
