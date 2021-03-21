package org.dicio.dicio_android.skills.fallback.text;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.FallbackSkill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

import java.util.List;

public class TextFallback implements FallbackSkill {

    @Override
    public void setInput(final String input,
                         final List<String> inputWords,
                         final List<String> normalizedWordKeys) {}

    @Override
    public void cleanup() {}

    @Override
    public void processInput(final SkillContext context) {}

    @Override
    public void generateOutput(final SkillContext context,
                               final SpeechOutputDevice speechOutputDevice,
                               final GraphicalOutputDevice graphicalOutputDevice) {

        final String noMatchString = context.getAndroidContext().getString(R.string.eval_no_match);
        speechOutputDevice.speak(noMatchString);
        graphicalOutputDevice.display(GraphicalOutputUtils.buildSubHeader(
                context.getAndroidContext(), noMatchString));
    }
}
