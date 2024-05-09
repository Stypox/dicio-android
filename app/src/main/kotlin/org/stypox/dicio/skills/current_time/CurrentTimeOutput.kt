package org.stypox.dicio.skills.current_time

import org.dicio.skill.chain.OutputGenerator
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.GraphicalOutputUtils

class CurrentTimeOutput : OutputGenerator<String?>() {
    override fun generate(timeStr: String?) {
        val message = ctx().android!!.getString(
            R.string.skill_time_current_time, timeStr
        )
        ctx().speechOutputDevice!!.speak(message)
        ctx().graphicalOutputDevice!!.display(
            GraphicalOutputUtils.buildSubHeader(
                ctx().android!!, message
            )
        )
    }
}
