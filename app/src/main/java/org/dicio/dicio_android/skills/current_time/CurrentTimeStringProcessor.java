package org.dicio.dicio_android.skills.current_time;

import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;

import android.content.res.Resources;
import java.util.Locale;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class CurrentTimeStringProcessor
    extends IntermediateProcessor<StandardResult, CurrentTimeOutput.Data> {
    @Override
    public CurrentTimeOutput.Data process(final StandardResult data) throws Exception {
        final CurrentTimeOutput.Data result = new CurrentTimeOutput.Data();
        final LocalTime rightNow = LocalTime.now();
        final Locale currentLocale = Resources.getSystem().getConfiguration().getLocales().get(0);

        final DateTimeFormatter formatter = DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(currentLocale);
        result.timeStr  = rightNow.format(formatter);
        return result;
    }
}
