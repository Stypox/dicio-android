package org.stypox.dicio.skills.current_time

import org.dicio.skill.chain.IntermediateProcessor
import org.dicio.skill.standard.StandardResult
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class CurrentTimeStringProcessor : IntermediateProcessor<StandardResult, String>() {
    @Throws(Exception::class)
    override fun process(data: StandardResult): String {
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(ctx().locale)
        return LocalTime.now().format(formatter)
    }
}