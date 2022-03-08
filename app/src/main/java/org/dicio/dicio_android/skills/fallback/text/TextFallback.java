package org.dicio.dicio_android.skills.fallback.text;

import androidx.annotation.Nullable;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.FallbackSkill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;

import java.util.List;

public class TextFallback extends FallbackSkill {

    public TextFallback(final SkillContext context, @Nullable final SkillInfo skillInfo) {
        super(context, skillInfo);
    }

    @Override
    public void setInput(final String input,
                         final List<String> inputWords,
                         final List<String> normalizedWordKeys) {
    }

    @Override
    public void cleanup() {
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
