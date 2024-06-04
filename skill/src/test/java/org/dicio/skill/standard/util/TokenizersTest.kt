package org.dicio.skill.standard.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.EqualityMatcherResult
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlin.math.abs

fun cumulativeWeight(userInput: String): FloatArray {
    val words = splitWords(userInput)
    return cumulativeWeight(userInput, words)
}

fun beEqualToPlusOrMinus(vararg expected: Float) = object : Matcher<FloatArray> {
    override fun test(value: FloatArray): MatcherResult {
        if (expected.size != value.size) {
            return EqualityMatcherResult.invoke(
                passed = false,
                actual = value,
                expected = expected,
                failureMessageFn = { "items have different lengths (expected size = ${
                    expected.size}, actual size = ${value.size})" },
                negatedFailureMessageFn = { "arrays are the same" }
            )
        }

        for (i in expected.indices) {
            if (abs(expected[i] - value[i]) > 0.0001f) {
                return EqualityMatcherResult.invoke(
                    passed = false,
                    actual = value,
                    expected = expected,
                    failureMessageFn = { "arrays differ at position $i (expected = ${
                        expected[i]}, actual = ${value[i]})" },
                    negatedFailureMessageFn = { "arrays are the same" }
                )
            }
        }

        return EqualityMatcherResult.invoke(
            passed = true,
            actual = value,
            expected = expected,
            failureMessageFn = { "arrays are different" },
            negatedFailureMessageFn = { "arrays are the same" }
        )
    }
}

class TokenizersTest : DescribeSpec({
    describe("splitWords") {
        it("empty") {
            splitWords("")
                .shouldBe(listOf())
        }
        it("blank") {
            splitWords(" \n\t 0,-7")
                .shouldBe(listOf())
        }
        it("whitespace") {
            splitWords(" hello how\nare \t  you ")
                .shouldBe(listOf(
                    WordToken(1, 6, "hello", "hello"),
                    WordToken(7, 10, "how", "how"),
                    WordToken(11, 14, "are", "are"),
                    WordToken(18, 21, "you", "you"),
                ))
        }
        it("punctuation") {
            splitWords("¿hello, .org!?")
                .shouldBe(listOf(
                    WordToken(1, 6, "hello", "hello"),
                    WordToken(9, 12, "org", "org"),
                ))
        }
        it("digits") {
            splitWords("he110 w0rld")
                .shouldBe(listOf(
                    WordToken(0, 2, "he", "he"),
                    WordToken(6, 7, "w", "w"),
                    WordToken(8, 11, "rld", "rld"),
                ))
        }
        it("case") {
            splitWords("HeLLo WoRlD")
                .shouldBe(listOf(
                    WordToken(0, 5, "hello", "hello"),
                    WordToken(6, 11, "world", "world"),
                ))
        }
        it("diacritics") {
            splitWords("hèlŁo wÒRłdç")
                .shouldBe(listOf(
                    WordToken(0, 5, "hèlło", "helło"),
                    WordToken(6, 12, "wòrłdç", "worłdc"),
                ))
        }
    }

    describe("cumulativeWeight") {
        it("empty") {
            cumulativeWeight("")
                .should(beEqualToPlusOrMinus(0.0f))
        }
        it("whitespace") {
            cumulativeWeight(" \n\t ")
                .should(beEqualToPlusOrMinus(0.0f, 0.0f, 0.0f, 0.0f, 0.0f))
        }
        it("punctuation") {
            cumulativeWeight(".,;-'")
                .should(beEqualToPlusOrMinus(0.0f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f))
        }
        it("digits") {
            cumulativeWeight("0123")
                .should(beEqualToPlusOrMinus(0.0f, 0.1f, 0.2f, 0.3f, 0.4f))
        }
        it("mixed") {
            cumulativeWeight(" \n.\t 0,-7")
                .should(beEqualToPlusOrMinus(0.0f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f, 0.15f, 0.2f,
                    0.25f, 0.35f))
        }
        it("words") {
            cumulativeWeight("hello guys")
                .should(beEqualToPlusOrMinus(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f, 1.0f, 1.25f, 1.5f,
                    1.75f, 2.0f))
        }
        it("long word") {
            cumulativeWeight("internationalisation")
                .should(beEqualToPlusOrMinus(0.0f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f, 0.35f,
                    0.4f, 0.45f, 0.5f, 0.55f, 0.6f, 0.65f, 0.7f, 0.75f, 0.8f, 0.85f, 0.9f, 0.95f,
                    1.0f))
        }
        it("mixed words") {
            cumulativeWeight("\nLala\thello, gu2ys!!")
                .should(beEqualToPlusOrMinus(0.0f, 0.0f, 0.25f, 0.5f, 0.75f, 1.0f, 1.0f, 1.2f, 1.4f,
                    1.6f, 1.8f, 2.0f, 2.05f, 2.05f, 2.55f, 3.05f, 3.15f, 3.65f, 4.15f, 4.20f,
                    4.25f))
        }
    }

    describe("cumulativeWhitespace") {
        it("only whitespace") {
            cumulativeWhitespace(" \n\t ")
                .shouldBe(arrayOf(0, 1, 2, 3, 4))
        }
        it("no whitespace") {
            cumulativeWhitespace("Hello!")
                .shouldBe(arrayOf(0, 0, 0, 0, 0, 0, 0))
        }
        it("mixed") {
            cumulativeWhitespace("\nHello, gu0ys\t!")
                .shouldBe(arrayOf(0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3))
        }
    }
})
