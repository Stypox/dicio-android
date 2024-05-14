package org.stypox.dicio.skills.current_time

import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput

class CurrentTimeGenerator : OutputGenerator<String>() {
    override fun generate(data: String): SkillOutput {
        return CurrentTimeOutput(data)
    }
}
