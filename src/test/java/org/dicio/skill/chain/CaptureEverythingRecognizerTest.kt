package org.dicio.skill.chain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.dicio.skill.chain.InputRecognizer.Specificity

class CaptureEverythingRecognizerTest : StringSpec({
    "specificity and score" {
        val cer = CaptureEverythingRecognizer()
        cer.specificity() shouldBeSameInstanceAs Specificity.LOW
        cer.score() shouldBe 1.0f
    }

    "input in capturing groups" {
        val input = "Some inpùt"
        val cer = CaptureEverythingRecognizer()

        cer.setInput(input, mutableListOf("Some", "inpùt"), mutableListOf("Some", "input"))
        cer.result.getCapturingGroup("") shouldBeSameInstanceAs input
        cer.result.getCapturingGroup("hello") shouldBeSameInstanceAs input
        cer.result.getCapturingGroup(input) shouldBeSameInstanceAs input
    }
})
