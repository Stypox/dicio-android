package org.dicio.dicio_android.skills.current_time;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.chain.OutputGenerator;

public class CurrentTimeOutput extends OutputGenerator<CurrentTimeOutput.Data> {
    public static class Data {
        public String timeStr;
    }

    @Override
    public void generate(final Data data) {
        final String message = ctx().android().getString(
                R.string.skill_time_current_time, data.timeStr);
        ctx().getSpeechOutputDevice().speak(message);
        ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                ctx().android(), message));
    }

    @Override
    public void cleanup() {
    }
}
