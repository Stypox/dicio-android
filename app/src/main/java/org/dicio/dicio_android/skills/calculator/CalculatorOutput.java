package org.dicio.dicio_android.skills.calculator;

import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

public class CalculatorOutput implements OutputGenerator<CalculatorOutput.Data> {

    public static class Data {
        String inputInterpretation;
        double result;
    }

    @Override
    public void generate(final Data data,
                         final SkillContext skillContext,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {
        graphicalOutputDevice.displayTemporary(GraphicalOutputUtils.buildDescription(skillContext.getAndroidContext(), data.inputInterpretation));
    }

    @Override
    public void cleanup() {

    }
}
