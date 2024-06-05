package org.dicio.skill.standard.construct

import io.kotest.core.spec.style.DescribeSpec

@Suppress("BooleanLiteralArgument")
class CompositeConstructTest : DescribeSpec({
    describe("empty or non-matching input") {
        it("empty input, with no constructs") {
            CompositeConstruct(listOf())
                .withInput("")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0f)
        }
        it("empty input, with some constructs") {
            val w1 = 1.4f
            val w2 = 1.0f
            CompositeConstruct(listOf(
                WordConstruct("a", false, false, w1),
                CapturingConstruct("name", w2),
            ))
                .withInput("")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = w1 + w2)
        }
        it("non-matching input, with no constructs") {
            CompositeConstruct(listOf())
                .withInput("whatever")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0f)
        }
        it("non-matching input, with some constructs") {
            val w1 = 1.4f
            val w2 = 1.0f
            CompositeConstruct(listOf(
                WordConstruct("a", false, false, w1),
                WordConstruct("b", false, false, w2),
            ))
                .withInput("whatever")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = w1 + w2)
        }
    }

    describe("something matches") {
        it("everything matches") {
            val w1 = 1.4f
            val w2 = 1.0f
            val w3 = 0.7f
            CompositeConstruct(listOf(
                WordConstruct("a", false, false, w1),
                WordConstruct("b", false, false, w2),
                WordConstruct("c", false, false, w3),
            ))
                .withInput("a b c")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(3f,3f,w1+w2+w3,w1+w2+w3),s(2f,2f,w2+w3,w1+w2+w3),
                        s(2f,2f,w2+w3,w1+w2+w3),s(1f,1f,w3,w1+w2+w3),s(1f,1f,w3,w1+w2+w3),s(0f,0f,0f,w1+w2+w3),)
        }
        it("both match, but not at the same time") {
            val w1 = 1.0f
            val w2 = 1.5f
            val n = "The capturing group name"
            CompositeConstruct(listOf(
                WordConstruct("a", false, false, w1),
                CapturingConstruct(n, w2)
            ))
                .withInput("a")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,1f,w2,w1+w2,capt(n,0,1)),s(0f,0f,0f,w1+w2))
        }
        it("single construct (capturing group)") {
            val w = 0.7f
            val n = "test"
            CompositeConstruct(listOf(CapturingConstruct(n, w)))
                .withInput(" a1b ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(2.1f,2.1f,w,w,capt(n,0,5)),s(2.1f,2.1f,w,w,capt(n,1,5)),
                    s(1.1f,1.1f,w,w,capt(n,2,5)),s(1f,1f,w,w,capt(n,3,5)),s(0f,0f,0f,w),s(0f,0f,0f,w))
        }
        it("single construct (word)") {
            val w = 1.2f
            CompositeConstruct(listOf(WordConstruct("a", false, false, w)))
                .withInput("b a.")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,2.05f,w,w),s(1f,1.05f,w,w),s(1f,1.05f,w,w),s(0f,0.05f,0f,w),s(0f,0f,0f,w))
        }
    }
})
