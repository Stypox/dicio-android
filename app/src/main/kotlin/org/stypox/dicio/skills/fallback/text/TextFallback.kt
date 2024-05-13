package org.stypox.dicio.skills.fallback.text

import org.dicio.skill.FallbackSkill
import org.dicio.skill.output.SkillOutput

class TextFallback : FallbackSkill() {
    override fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ) {}

    override fun processInput() {}

    override fun generateOutput(): SkillOutput {
        return TextFallbackOutput(ctx().android!!)
    }
}
