package org.dicio.dicio_android.skills.calculator;

import org.dicio.dicio_android.Sections;
import org.dicio.dicio_android.SectionsGenerated;
import org.dicio.numbers.util.Number;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.skill.standard.StandardResult;
import org.dicio.skill.util.WordExtractor;

import java.util.ArrayList;
import java.util.List;

public class CalculatorProcessor
        implements IntermediateProcessor<StandardResult, CalculatorOutput.Data> {

    private String getOperation(final StandardRecognizer operatorRecognizer,
                                final String text) {
        final List<String> inputWords = WordExtractor.extractWords(text);
        final List<String> normalizedWordKeys = WordExtractor.normalizeWords(inputWords);
        operatorRecognizer.setInput(text, inputWords, normalizedWordKeys);

        if (operatorRecognizer.score() < 0.3) {
            return ""; // prevent bad matches
        } else {
            return operatorRecognizer.getResult().getSentenceId();
        }
    }

    @Override
    public CalculatorOutput.Data process(final StandardResult data,
                                         final SkillContext skillContext) {
        final CalculatorOutput.Data result = new CalculatorOutput.Data();

        final List<Object> textWithNumbers = skillContext.getNumberParserFormatter()
                .extractNumbers(data.getCapturingGroup("calculation")).get();
        if (textWithNumbers.size() == 0
                || (textWithNumbers.size() == 1 && !(textWithNumbers.get(0) instanceof Number))) {
            result.failed = true;
            return result;
        }

        final StandardRecognizer operatorRecognizer = new StandardRecognizer(
                Sections.getSection(SectionsGenerated.calculator_operators));

        int i;
        if (textWithNumbers.get(0) instanceof Number) {
            result.number = (Number) textWithNumbers.get(0);
            i = 1;
        } else {
            result.number = (Number) textWithNumbers.get(1);
            if (getOperation(operatorRecognizer, (String) textWithNumbers.get(0))
                    .equals("subtraction")) {
                result.number = result.number.multiply(-1);
            }
            i = 2;
        }

        result.inputInterpretation = new ArrayList<>();
        result.inputInterpretation.add(result.number);

        while (i < textWithNumbers.size()) {
            final String operation;
            if (textWithNumbers.get(i) instanceof Number) {
                operation = "addition"; // add two subsequent numbers
            } else if (i + 1 < textWithNumbers.size()) {
                operation = getOperation(operatorRecognizer, (String) textWithNumbers.get(i));
                ++i;
            } else {
                break;
            }

            Number number = (Number) textWithNumbers.get(i);
            ++i;

            switch (operation) {
                default: // default to addition
                case "addition":
                    result.inputInterpretation.add(number.lessThan(0) ? "-" : "+");
                    result.number = result.number.plus(number);
                    number = number.lessThan(0) ? number.multiply(-1) : number;
                    break;
                case "subtraction":
                    result.inputInterpretation.add("-");
                    result.number = result.number.plus(number.multiply(-1));
                    break;
                case "multiplication":
                    result.inputInterpretation.add("x");
                    result.number = result.number.multiply(number);
                    break;
                case "division":
                    result.inputInterpretation.add("÷");
                    result.number = result.number.divide(number);
                    break;
                case "power":
                    result.inputInterpretation.add("^");
                    if (result.number.equals(0)) {
                        if (number.equals(0)) {
                            result.number = new Number(Double.NaN);
                        }
                        break;
                    } else if (result.number.equals(1)) {
                        break;
                    }

                    final long sign = result.number.lessThan(0) ? -1 : 1;
                    if (result.number.isDecimal() || number.isDecimal() || number.lessThan(0)) {
                        final double valueBefore = Math.abs(result.number.isDecimal() ?
                                result.number.decimalValue() : result.number.integerValue());
                        final double valueAfter =
                                number.isDecimal() ? number.decimalValue() : number.integerValue();
                        result.number = new Number(Math.pow(valueBefore, valueAfter) * sign);
                    } else {
                        final Number before = result.number;
                        result.number = new Number(1);
                        for (long j = 0; j < number.integerValue(); ++j) {
                            result.number = result.number.multiply(before);
                        }
                        result.number = result.number.multiply(sign);
                    }
                    break;
            }
            result.inputInterpretation.add(number);
        }

        operatorRecognizer.cleanup();
        return result;
    }
}
