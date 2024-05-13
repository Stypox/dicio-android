package org.stypox.dicio.skills.current_time

import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.GraphicalOutputUtils

class CurrentTimeGenerator : OutputGenerator<String>() {
    override fun generate(data: String): SkillOutput {
        return CurrentTimeOutput(data)
    }
}
