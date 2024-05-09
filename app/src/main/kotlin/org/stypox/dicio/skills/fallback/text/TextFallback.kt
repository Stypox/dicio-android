package org.stypox.dicio.skills.fallback.text

import org.dicio.skill.FallbackSkill
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.GraphicalOutputUtils

class TextFallback : FallbackSkill() {
    override fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ) {
    }

    override fun processInput() {}
    override fun generateOutput() {
        val noMatchString = ctx().android!!.getString(R.string.eval_no_match)
        ctx().speechOutputDevice!!.speak(noMatchString)
        ctx().graphicalOutputDevice!!.display(
            GraphicalOutputUtils.buildSubHeader(
                ctx().android!!, noMatchString
            )
        )
    }
}
