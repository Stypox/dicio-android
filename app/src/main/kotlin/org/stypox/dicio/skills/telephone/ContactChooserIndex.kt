package org.stypox.dicio.skills.telephone

import org.dicio.numbers.unit.Number
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.skill.Specificity

class ContactChooserIndex internal constructor(private val contacts: List<Pair<String, String>>) :
    Skill<Int>(TelephoneInfo, Specificity.HIGH) {

    override fun score(
        ctx: SkillContext,
        input: String
    ): Pair<Float, Int> {
        val index = ctx.parserFormatter!!
            .extractNumber(input)
            .preferOrdinal(true)
            .mixedWithText
            .asSequence()
            .filter { obj -> (obj as? Number)?.isInteger == true }
            .map { obj -> (obj as Number).integerValue().toInt() }
            .firstOrNull() ?: 0
        return Pair(
            if (index <= 0 || index > contacts.size) 0.0f else 1.0f,
            index
        )
    }

    override suspend fun generateOutput(ctx: SkillContext, scoreResult: Int): SkillOutput {
        if (scoreResult > 0 && scoreResult <= contacts.size) {
            val contact = contacts[scoreResult - 1]
            return ConfirmCallOutput(contact.first, contact.second)
        } else {
            // impossible situation
            return ConfirmedCallOutput(null)
        }
    }
}
