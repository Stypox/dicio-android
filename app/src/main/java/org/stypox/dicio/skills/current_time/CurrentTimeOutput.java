package org.stypox.dicio.skills.current_time;

import org.stypox.dicio.R;
import org.stypox.dicio.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.chain.OutputGenerator;

public class CurrentTimeOutput extends OutputGenerator<String> {
    @Override
    public void generate(final String timeStr) {
        final String message = ctx().android().getString(
                R.string.skill_time_current_time, timeStr);
        ctx().getSpeechOutputDevice().speak(message);
        ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                ctx().android(), message));
    }

    @Override
    public void cleanup() {
    }
}
