package org.stypox.dicio.skills.telephone

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.skill.Specificity
import org.stypox.dicio.util.StringUtils

class ContactChooserName internal constructor(private val contacts: List<Pair<String, String>>) :
    // use a low specificity to prefer the index-based contact chooser
    Skill<Pair<String, String>?>(TelephoneInfo, Specificity.LOW) {

    override fun score(
        ctx: SkillContext,
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ): Pair<Float, Pair<String, String>?> {
        val trimmedInput = input.trim { it <= ' ' }

        val bestContact = contacts
            .map { nameNumberPair ->
                Pair(
                    nameNumberPair,
                    StringUtils.contactStringDistance(trimmedInput, nameNumberPair.first)
                )
            }
            .filter { pair -> pair.second < -7 }
            .minByOrNull { a -> a.second }
            ?.first

        return Pair(
            if (bestContact == null) 0.0f else 1.0f,
            bestContact
        )
    }

    override suspend fun generateOutput(ctx: SkillContext, scoreResult: Pair<String, String>?): SkillOutput {
        return scoreResult?.let {
            ConfirmCallOutput(it.first, it.second)
        }
            // impossible situation
            ?: ConfirmedCallOutput(null)
    }
}
