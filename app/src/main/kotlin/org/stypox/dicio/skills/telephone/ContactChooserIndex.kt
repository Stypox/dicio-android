package org.stypox.dicio.skills.telephone

import org.dicio.numbers.util.Number
import org.dicio.skill.Skill
import org.dicio.skill.chain.InputRecognizer.Specificity

class ContactChooserIndex internal constructor(private val contacts: List<NameNumberPair>) :
    Skill() {
    private var input: String? = null
    private var index = 0
    override fun specificity(): Specificity {
        return Specificity.high
    }

    override fun setInput(
        theInput: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ) {
        input = theInput
    }

    override fun score(): Float {
        index = ctx().requireNumberParserFormatter()
            .extractNumbers(input)
            .preferOrdinal(true)
            .get().stream()
            .filter { obj: Any? -> Number::class.java.isInstance(obj) }
            .map { obj: Any? -> Number::class.java.cast(obj) }
            .filter { obj: Number -> obj.isInteger }
            .mapToInt { number: Number -> number.integerValue().toInt() }
            .findFirst()
            .orElse(0)
        return if (index <= 0 || index > contacts.size) 0.0f else 1.0f
    }

    override fun processInput() {}
    override fun generateOutput() {
        if (index > 0 && index <= contacts.size) {
            val contact = contacts[index - 1]
            ConfirmCallOutput.callAfterConfirmation(
                this,
                contact.name,
                contact.number
            )
        }
    }

    override fun cleanup() {
        super.cleanup()
        input = null
    }
}