package org.stypox.dicio.skills.timer

import org.dicio.skill.chain.IntermediateProcessor
import org.dicio.skill.standard.StandardResult

class TimerProcessor : IntermediateProcessor<StandardResult, TimerGenerator.Data>() {
    @Throws(Exception::class)
    override fun process(data: StandardResult): TimerGenerator.Data {
        return TimerGenerator.Data(
            action = when (data.sentenceId) {
                "set" -> TimerGenerator.Action.SET
                "cancel" -> TimerGenerator.Action.CANCEL
                "query" -> TimerGenerator.Action.QUERY
                else -> TimerGenerator.Action.SET
            },
            duration = data.getCapturingGroup("duration")?.let {
                ctx().parserFormatter!!.extractDuration(it).first?.toJavaDuration()
            },
            name = data.getCapturingGroup("name"),
        )
    }
}
