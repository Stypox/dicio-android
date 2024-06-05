package org.dicio.skill.standard.construct

import io.kotest.core.spec.style.DescribeSpec

class CapturingConstructTest : DescribeSpec({
    describe("capturing group does not fit") {
        it("empty input") {
            val w = 0.5f
            CapturingConstruct("name", w)
                .withInput("")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = w)
        }
        it("only whitespace, which is not captured") {
            val n = "the capturing group name"
            val w = 1.5f
            CapturingConstruct(n, w)
                .withInput(" \n\r ")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = w)
        }
        it("everything already matched well") {
            // The starting memToEnd is as if 4 single-character constructs with weight 1.0f were
            // already passed through it. Therefore set the capturing group's weight
            // lower than 1f, otherwise the capturing group would always take a char.
            val w = 0.6f
            CapturingConstruct("The_Name", w)
                .withInput("aaaa")
                .withStartingMemToEnd(s(1f,1f,4f,4f),s(0.75f,0.75f,3f,4f),s(0.5f,0.5f,2f,4f),s(0.25f,0.25f,1f,4f),s(0f,0f,0f,4f))
                .shouldNotMatchAnything(additionalRefWeight = w)
        }
    }

    describe("things are captured") {
        it("all surrounding whitespace is captured") {
            val n = ""
            val w = 0.5f
            CapturingConstruct(n, w)
                .withInput(" a. ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1.05f,1.05f,w,w,capt(n,0,4)),s(1.05f,1.05f,w,w,capt(n,1,4)),s(0.05f,0.05f,w,w,capt(n,2,4)),s(0f,0f,0f,w),s(0f,0f,0f,w))
        }
        it("capturing group is best from any position") {
            val n = "myname"
            val w = 1.0f // use the standard weight here
            CapturingConstruct(n, w)
                .withInput("a b c")
                .withStartingMemToEnd(s(1f,3f,1f,1f),s(0f,2f,0f,1f),s(0f,2f,0f,1f),s(0f,1f,0f,1f),s(0f,1f,0f,1f),s(0f,0f,0f,1f))
                .shouldChangeMemToEndInto(s(3f,3f,w,1f+w,capt(n,0,5)),s(2f,2f,w,1f+w,capt(n,1,5)),s(2f,2f,w,1f+w,capt(n,2,5)),s(1f,1f,w,1f+w,capt(n,3,5)),s(1f,1f,w,1f+w,capt(n,4,5)),s(0f,0f,0f,1f+w))
        }
        it("capturing group does not expand more than needed") {
            val n = "test"
            // The starting memToEnd is as if 4 single-character constructs with weight 1.0f were
            // already passed through it. Therefore set the capturing group's weight
            // higher than 1f, but lower than 1f, to capture a char at a time.
            val w = 1.7f
            CapturingConstruct(n, w)
                .withInput("aaaa")
                .withStartingMemToEnd(s(1f,1f,4f,4f),s(0.75f,0.75f,3f,4f),s(0.5f,0.5f,2f,4f),s(0.25f,0.25f,1f,4f),s(0f,0f,0f,4f))
                .shouldChangeMemToEndInto(s(1f,1f,3f+w,4f+w,capt(n,0,1)),s(0.75f,0.75f,2f+w,4f+w,capt(n,1,2)),s(0.5f,0.5f,1f+w,4f+w,capt(n,2,3)),s(0.25f,0.25f,w,4f+w,capt(n,3,4)),s(0f,0f,0f,4f+w))
        }
        it("last word already matched well") {
            val n = "12345"
            val w = 0.9f
            CapturingConstruct(n, w)
                .withInput("b c")
                .withStartingMemToEnd(s(1f,2f,1f,1f),s(1f,1f,1f,1f),s(1f,1f,1f,1f),s(0f,0f,0f,1f))
                .shouldChangeMemToEndInto(s(2f,2f,1f+w,1f+w,capt(n,0,2)),s(1f,1f,1f,1f+w),s(1f,1f,1f,1f+w),s(0f,0f,0f,1f+w))
        }
    }

    describe("multiple capturing groups") {
        it("previous captures may be overwritten if better choices arise") {
            val n1 = "1"
            val n2 = "2"
            val n3 = "3"
            val w = 1.5f // higher than 1f, i.e. the weight of n2 and n3
            CapturingConstruct(n1, w)
                .withInput("b c")
                .withStartingMemToEnd(s(2f,2f,2f,2f,capt(n2,0,2),capt(n3,2,3)),s(1f,1f,1f,2f,capt(n3,1,3)),s(1f,1f,1f,2f,capt(n3,2,3)),s(0f,0f,0f,2f))
                .shouldChangeMemToEndInto(s(2f,2f,1f+w,2f+w,capt(n1,0,2),capt(n3,2,3)),s(1f,1f,w,2f+w,capt(n1,2,3)),s(1f,1f,w,2f+w,capt(n1,2,3)),s(0f,0f,0f,2f+w))
        }
        it("previous captures are preserved") {
            val n1 = "1"
            val n2 = "2"
            val w = 0.5f // lower than 1f, i.e. the weight of n2
            CapturingConstruct(n1, w)
                .withInput("b c")
                .withStartingMemToEnd(s(2f,2f,1f,1f,capt(n2,0,3)),s(1f,1f,1f,1f,capt(n2,1,3)),s(1f,1f,1f,1f,capt(n2,2,3)),s(0f,0f,0f,1f))
                .shouldChangeMemToEndInto(s(2f,2f,1f+w,1f+w,capt(n1,0,2),capt(n2,2,3)),s(1f,1f,1f,1f+w,capt(n2,1,3)),s(1f,1f,1f,1f+w,capt(n2,2,3)),s(0f,0f,0f,1f+w))
        }
        it("two capturing groups with word in the middle") {
            val n1 = "1"
            val n3 = "3"
            val w1 = 1.5f
            val w2 = 0.5f
            val w3 = 1.0f
            val userInput = "a b c"
            val after1 = arrayOf(s(3f,3f,w1,w1,capt(n1,0,5)),s(2f,2f,w1,w1,capt(n1,1,5)),s(2f,2f,w1,w1,capt(n1,2,5)),
                                 s(1f,1f,w1,w1,capt(n1,3,5)),s(1f,1f,w1,w1,capt(n1,4,5)),s(0f,0f,0f,w1             ))
            val after2 = arrayOf(s(3f,3f,w1,w1+w2,capt(n1,0,5)),s(2f,2f,w1+w2,w1+w2,capt(n1,3,5)),s(2f,2f,w1+w2,w1+w2,capt(n1,3,5)),
                                 s(1f,1f,w1,w1+w2,capt(n1,3,5)),s(1f,1f,w1,   w1+w2,capt(n1,4,5)),s(0f,0f,0f,   w1+w2             ))
            val after3 = arrayOf(s(3f,3f,w1+w2+w3,w1+w2+w3,capt(n3,0,2),capt(n1,3,5)),s(2f,2f,w1+w3,w1+w2+w3,capt(n3,2,4),capt(n1,4,5)),s(2f,2f,w1+w3,w1+w2+w3,capt(n3,2,4),capt(n1,4,5)),
                                 s(1f,1f,w1,      w1+w2+w3,capt(n1,3,5)),             s(1f,1f,w1,w1+w2+w3,                capt(n1,4,5)),s(0f,0f,0f,   w1+w2+w3                          ))

            CapturingConstruct(n1, w1)
                .withInput(userInput)
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(*after1)

            WordConstruct(text = "b", isRegex = false, isDiacriticsSensitive = false, weight = w2)
                .withInput(userInput)
                .withStartingMemToEnd(*after1)
                .shouldChangeMemToEndInto(*after2)

            CapturingConstruct(n3, w3)
                .withInput(userInput)
                .withStartingMemToEnd(*after2)
                .shouldChangeMemToEndInto(*after3)
        }
    }
})
