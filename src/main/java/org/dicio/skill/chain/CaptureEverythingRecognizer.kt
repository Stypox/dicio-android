package org.dicio.skill.chain

import org.dicio.skill.standard.StandardResult

/**
 * A recognizer that always matches any input and results in a [StandardResult] on which
 * calling [StandardResult.getCapturingGroup] always returns the input. The
 * specificity is LOW and the score is always 1.0.
 */
class CaptureEverythingRecognizer : InputRecognizer<StandardResult>(Specificity.LOW) {
    private var input: String? = null

    override fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedInputWords: List<String>
    ) {
        this.input = input
    }

    override fun score(): Float {
        return 1.0f
    }

    override val result: StandardResult
        get() = object : StandardResult("", input!!, mapOf()) {
            override fun getCapturingGroup(name: String): String = input!!
        }

    override fun cleanup() {
    }
}
