package org.dicio.dicio_android.skills.current_time;

import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;

import java.util.Locale;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class CurrentTimeStringProcessor
    extends IntermediateProcessor<StandardResult, String> {
    @Override
    public String process(final StandardResult data) throws Exception {
        String result = new String();
        final LocalTime rightNow = LocalTime.now();
        final Locale currentLocale = ctx().getLocale();

        final DateTimeFormatter formatter = DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(currentLocale);
        result  = rightNow.format(formatter);
        return result;
    }
}
