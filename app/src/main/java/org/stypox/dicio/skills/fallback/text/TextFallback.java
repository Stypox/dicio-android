package org.stypox.dicio.skills.fallback.text;

import org.stypox.dicio.R;
import org.stypox.dicio.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.FallbackSkill;

import java.util.List;

public class TextFallback extends FallbackSkill {

    @Override
    public void setInput(final String input,
                         final List<String> inputWords,
                         final List<String> normalizedWordKeys) {
    }

    @Override
    public void processInput() {
    }

    @Override
    public void generateOutput() {
        final String noMatchString = ctx().android().getString(R.string.eval_no_match);
        ctx().getSpeechOutputDevice().speak(noMatchString);
        ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                ctx().android(), noMatchString));
    }
}
