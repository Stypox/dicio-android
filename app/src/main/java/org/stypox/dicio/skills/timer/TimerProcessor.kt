package org.stypox.dicio.skills.timer

import org.dicio.skill.chain.IntermediateProcessor
import org.dicio.skill.standard.StandardResult

class TimerProcessor : IntermediateProcessor<StandardResult, TimerOutput.Data>() {
    @Throws(Exception::class)
    override fun process(data: StandardResult): TimerOutput.Data {
        return TimerOutput.Data(
            action = when (data.sentenceId) {
                "set" -> TimerOutput.Action.SET
                "cancel" -> TimerOutput.Action.CANCEL
                "query" -> TimerOutput.Action.QUERY
                else -> TimerOutput.Action.SET
            },
            duration = data.getCapturingGroup("duration").let {
                ctx().requireNumberParserFormatter().extractDuration(it).get()
            },
            name = data.getCapturingGroup("name"),
        )
    }
}