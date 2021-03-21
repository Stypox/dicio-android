package org.dicio.dicio_android.skills.calculator;

import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;

import java.util.List;

public class CalculatorProcessor
        implements IntermediateProcessor<StandardResult, CalculatorOutput.Data> {

    @Override
    public CalculatorOutput.Data process(final StandardResult data, final SkillContext skillContext)
            throws Exception {
        final List<Object> textWithNumbers = skillContext.getNumberParserFormatter()
                .extractNumbers(data.getCapturingGroup("calculation")).get();
        final CalculatorOutput.Data result = new CalculatorOutput.Data();
        result.inputInterpretation = textWithNumbers.toString();
        return result;
    }
}
