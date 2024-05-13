package org.stypox.dicio.skills.telephone

import org.dicio.skill.Skill
import org.dicio.skill.chain.InputRecognizer.Specificity
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.util.StringUtils

class ContactChooserName internal constructor(private val contacts: List<Pair<String, String>>) :
    Skill() {
    private var input: String? = null
    private var bestContact: Pair<String, String>? = null
    override fun specificity(): Specificity {
        // use a low specificity to prefer the index-based contact chooser
        return Specificity.LOW
    }

    override fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ) {
        this.input = input
    }

    override fun score(): Float {
        val input = input?.trim { it <= ' ' } ?: return 0.0f

        bestContact = contacts
            .map { nameNumberPair ->
                Pair(
                    nameNumberPair,
                    StringUtils.contactStringDistance(input, nameNumberPair.first)
                )
            }
            .filter { pair -> pair.second < -7 }
            .minByOrNull { a -> a.second }
            ?.first

        return if (bestContact == null) 0.0f else 1.0f
    }

    override fun processInput() {}
    override fun generateOutput(): SkillOutput {
        return bestContact?.let {
            ConfirmCallOutput(ctx().android!!, it.first, it.second)
        }
            // impossible situation
            ?: ConfirmedCallOutput(ctx().android!!, null)
    }

    override fun cleanup() {
        super.cleanup()
        bestContact = null
    }
}
