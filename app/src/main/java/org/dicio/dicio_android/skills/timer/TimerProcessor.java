package org.dicio.dicio_android.skills.timer;

import androidx.annotation.Nullable;

import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;

public class TimerProcessor extends IntermediateProcessor<StandardResult, TimerOutput.Data> {

    @Override
    public TimerOutput.Data process(final StandardResult data) throws Exception {
        final TimerOutput.Data result = new TimerOutput.Data();

        switch (data.getSentenceId()) {
            case "set": default:
                result.action = TimerOutput.Action.set;
                break;
            case "cancel":
                result.action = TimerOutput.Action.cancel;
                break;
            case "query":
                result.action = TimerOutput.Action.query;
                break;
        }

        @Nullable final String durationString = data.getCapturingGroup("duration");
        if (durationString != null) {
            result.duration = ctx().requireNumberParserFormatter()
                    .extractDuration(durationString).get();
        }

        result.name = data.getCapturingGroup("name");

        return result;
    }
}
