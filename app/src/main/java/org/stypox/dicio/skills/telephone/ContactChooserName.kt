package org.stypox.dicio.skills.telephone

import org.dicio.skill.Skill
import org.dicio.skill.chain.InputRecognizer.Specificity
import org.stypox.dicio.util.StringUtils

class ContactChooserName internal constructor(private val contacts: List<NameNumberPair>) :
    Skill() {
    private var input: String? = null
    private var bestContact: NameNumberPair? = null
    override fun specificity(): Specificity {
        // use a low specificity to prefer the index-based contact chooser
        return Specificity.low
    }

    override fun setInput(
        theInput: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ) {
        input = theInput
    }

    override fun score(): Float {
        class Pair(val nameNumberPair: NameNumberPair, val distance: Int)
        val input = input?.trim { it <= ' ' } ?: return 0.0f

        bestContact = contacts
            .map { nameNumberPair: NameNumberPair ->
                Pair(
                    nameNumberPair,
                    StringUtils.contactStringDistance(input, nameNumberPair.name)
                )
            }
            .filter { pair -> pair.distance < -7 }
            .minByOrNull { a -> a.distance }
            ?.nameNumberPair

        return if (bestContact == null) 0.0f else 1.0f
    }

    override fun processInput() {}
    override fun generateOutput() {
        bestContact?.also { ConfirmCallOutput.callAfterConfirmation(this, it.name, it.number) }
    }

    override fun cleanup() {
        super.cleanup()
        bestContact = null
    }
}