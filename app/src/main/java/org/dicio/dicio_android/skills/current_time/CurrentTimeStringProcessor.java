package org.dicio.dicio_android.skills.current_time;

import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class CurrentTimeStringProcessor
    extends IntermediateProcessor<StandardResult, String> {
    @Override
    public String process(final StandardResult data) throws Exception {
        final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(ctx().getLocale());
        return LocalTime.now().format(formatter);
    }
}
