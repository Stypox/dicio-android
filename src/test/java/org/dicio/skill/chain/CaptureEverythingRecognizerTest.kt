package org.dicio.skill.chain

import org.dicio.skill.chain.InputRecognizer.Specificity
import org.junit.Assert
import org.junit.Test

class CaptureEverythingRecognizerTest {
    @Test
    fun testSpecificityScore() {
        val cer = CaptureEverythingRecognizer()
        Assert.assertEquals(Specificity.LOW, cer.specificity())
        Assert.assertEquals(1.0f, cer.score(), 0.0f)
    }

    @Test
    fun testInput() {
        val input = "Some inpùt"
        val cer = CaptureEverythingRecognizer()

        cer.setInput(input, mutableListOf("Some", "inpùt"), mutableListOf("Some", "input"))
        Assert.assertSame(input, cer.result.getCapturingGroup(""))
        Assert.assertSame(input, cer.result.getCapturingGroup("hello"))
        Assert.assertSame(input, cer.result.getCapturingGroup(input))
    }
}
