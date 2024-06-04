package org.dicio.skill.standard.construct

import io.kotest.core.spec.style.DescribeSpec

@Suppress("BooleanLiteralArgument")
class WordConstructTest : DescribeSpec({
    describe("normal word") {
        it("empty input") {
            val w = 1.0f
            WordConstruct("hello", false, false, w)
                .withInput("")
                .withStartingZeroedMemToEnd()
                .shouldChangeMemToEndInto(s(0f,0f,0f,w))
        }
        it("no words, starting zeroed memToEnd") {
            val w = 1.7f
            WordConstruct("hello", false, false, w)
                .withInput("8 6; 9,2")
                .withStartingZeroedMemToEnd()
                .shouldChangeMemToEndInto(*Array(9) { s(0f,0f,0f,w) })
        }
        it("no words, starting random memToEnd") {
            val w = 2.3f
            val starting = arrayOf(s(0.4f,0.4f,2.0f,2.0f),s(0.3f,0.3f,1.5f,1.5f),s(0.2f,0.2f,1.0f,1.5f),s(0.0f,0.1f,0.0f,1.5f),s(0.0f,0.0f,0.0f,1.5f))
            val changed = starting.map { it.plus(refWeight = w) }.toTypedArray()
            WordConstruct("hello", false, false, w)
                .withInput("1234")
                .withStartingMemToEnd(*starting)
                .shouldChangeMemToEndInto(*changed)
        }
        it("no matching words, starting zeroed memToEnd") {
            val w = 0.5f
            WordConstruct("hello", false, false, w)
                .withInput("a aa abc hallo")
                .withStartingZeroedMemToEnd()
                .shouldChangeMemToEndInto(*Array(15) { s(0f,0f,0f,w) })
        }
        it("no matching words, starting random memToEnd") {
            val w = 1.1f
            val starting = arrayOf(s(0.4f,0.4f,2.0f,2.0f),s(0.3f,0.3f,1.5f,1.5f),s(0.2f,0.2f,1.0f,1.5f),s(0.0f,0.1f,0.0f,1.5f),s(0.0f,0.0f,0.0f,1.5f))
            val changed = starting.map { it.plus(refWeight = w) }.toTypedArray()
            WordConstruct("hello", false, false, w)
                .withInput("a aa")
                .withStartingMemToEnd(*starting)
                .shouldChangeMemToEndInto(*changed)
        }
        it("matching word, starting initial mem to end") {
            val w = 1.3f
            WordConstruct("a", false, false, w)
                .withInput("a aa a")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,3f,w,w),s(1f,2f,w,w),s(1f,2f,w,w),s(1f,1.5f,w,w),s(1f,1f,w,w),s(1f,1f,w,w),s(0f,0f,0f,w))
        }
        it("matching word, with spaces around, starting initial mem to end") {
            val w = 0.8f
            WordConstruct("a", false, false, w)
                .withInput(" a a ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,2f,w,w),s(1f,2f,w,w),s(1f,1f,w,w),s(1f,1f,w,w),s(0f,0f,0f,w),s(0f,0f,0f,w))
        }
        it("matching word with diacritics and different case and punctuation") {
            val w = 1.7f
            for (letter in "eEèÈéÉ") {
                WordConstruct("e", false, false, w)
                    .withInput(".a.$letter.a.")
                    .withStartingInitialMemToEnd()
                    .shouldChangeMemToEndInto(s(1f,3.2f,w,w),s(1f,3.15f,w,w),s(1f,2.15f,w,w),s(1f,2.1f,w,w),s(0f,1.1f,0f,w),s(0f,1.05f,0f,w),s(0f,0.05f,0f,w),s(0f,0f,0f,w))
            }
        }
        it("two words in a row") {
            val w1 = 1.1f
            val w2 = 0.7f
            val intermediateMemToEnd = arrayOf(s(1f,2f,w1,w1),s(1f,2f,w1,w1),s(1f,1f,w1,w1),s(1f,1f,w1,w1),s(0f,0.5f,0f,w1),s(0f,0f,0f,w1))

            WordConstruct("aa", false, false, w1)
                .withInput(" a aa")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(*intermediateMemToEnd)

            WordConstruct("a", false, false, w2)
                .withInput(" a aa")
                .withStartingMemToEnd(*intermediateMemToEnd)
                .shouldChangeMemToEndInto(s(2f,2f,w1+w2,w1+w2),s(2f,2f,w1+w2,w1+w2),s(1f,1f,w1,w1+w2),s(1f,1f,w1,w1+w2),s(0f,0.5f,0f,w1+w2),s(0f,0f,0f,w1+w2))
        }
    }

    describe("diacritics sensitive word") {
        fun onlyTheMiddleWordMatchedMemToEnd(w: Float) = arrayOf(
            s(1f,3f,w,w),s(1f,2f,w,w),s(1f,2f,w,w),s(0f,1f,0f,w),s(0f,1f,0f,w),s(0f,0f,0f,w)
        )
        it("WordConstruct's text has no diacritics, lowercase input") {
            val w = 0.2f
            WordConstruct("e", false, true, w)
                .withInput("è e é")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(*onlyTheMiddleWordMatchedMemToEnd(w))
        }
        it("WordConstruct's text has no diacritics, uppercase input") {
            val w = 0.8f
            WordConstruct("e", false, true, w)
                .withInput("É E È")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(*onlyTheMiddleWordMatchedMemToEnd(w))
        }
        it("WordConstruct's text has diacritics, lowercase input") {
            val w = 2.1f
            WordConstruct("è", false, true, w)
                .withInput("e è é")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(*onlyTheMiddleWordMatchedMemToEnd(w))
        }
        it("WordConstruct's text has diacritics, uppercase input") {
            val w = 1.5f
            WordConstruct("é", false, true, w)
                .withInput("È É E")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(*onlyTheMiddleWordMatchedMemToEnd(w))
        }
    }

    describe("regex word") {
        it("input has no diacritics") {
            val w = 0.4f
            WordConstruct("a(?:b|c)", true, false, w)
                .withInput(" ab ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,1f,w,w),s(1f,1f,w,w),s(0f,0.5f,0f,w),s(0f,0f,0f,w),s(0f,0f,0f,w))
        }
        it("input has diacritics") {
            val w = 0.6f
            WordConstruct("a(?:b|c)", true, false, w)
                .withInput(" Àç ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,1f,w,w),s(1f,1f,w,w),s(0f,0.5f,0f,w),s(0f,0f,0f,w),s(0f,0f,0f,w))
        }
        it("input doesn't match") {
            val w = 0.6f
            WordConstruct("a(?:b|c)", true, false, w)
                .withInput(" abc ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(0f,1f,0f,w),s(0f,1f,0f,w),s(0f,2f/3f,0f,w),s(0f,1f/3f,0f,w),s(0f,0f,0f,w),s(0f,0f,0f,w))
        }
    }

    describe("diacritics sentitive regex word") {
        it("input has correct diacritics") {
            val w = 0.8f
            WordConstruct("a(?:b|ç)", true, true, w)
                .withInput(" aç ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(1f,1f,w,w),s(1f,1f,w,w),s(0f,0.5f,0f,w),s(0f,0f,0f,w),s(0f,0f,0f,w))
        }
        it("input has incorrect diacritics") {
            val w = 0.6f
            WordConstruct("à(?:b|c)", true, true, w)
                .withInput(" Àç ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(0f,1f,0f,w),s(0f,1f,0f,w),s(0f,0.5f,0f,w),s(0f,0f,0f,w),s(0f,0f,0f,w))
        }
        it("input doesn't match regex") {
            val w = 0.6f
            WordConstruct("à(?:b|ç)", true, true, w)
                .withInput(" àbç ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(0f,1f,0f,w),s(0f,1f,0f,w),s(0f,2f/3f,0f,w),s(0f,1f/3f,0f,w),s(0f,0f,0f,w),s(0f,0f,0f,w))
        }
    }
})
