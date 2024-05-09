package org.stypox.dicio.skills.telephone

import org.dicio.numbers.unit.Number
import org.dicio.skill.Skill
import org.dicio.skill.chain.InputRecognizer.Specificity

class ContactChooserIndex internal constructor(private val contacts: List<NameNumberPair>) :
    Skill() {
    private var input: String? = null
    private var index = 0
    override fun specificity(): Specificity {
        return Specificity.HIGH
    }

    override fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ) {
        this.input = input
    }

    override fun score(): Float {
        index = ctx().parserFormatter!!
            .extractNumber(input)
            .preferOrdinal(true)
            .mixedWithText
            .asSequence()
            .filter { obj -> (obj as? Number)?.isInteger == true }
            .map { obj -> (obj as Number).integerValue().toInt() }
            .firstOrNull() ?: 0
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
