package org.dicio.dicio_android.skills.calculator;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.dicio.dicio_android.Sections;
import org.dicio.dicio_android.SectionsGenerated;
import org.dicio.numbers.util.Number;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.skill.standard.StandardResult;
import org.dicio.skill.util.WordExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Number firstNumber;
        int i;
        if (textWithNumbers.get(0) instanceof Number) {
            firstNumber = (Number) textWithNumbers.get(0);
            i = 1;
        } else {
            firstNumber = (Number) textWithNumbers.get(1);
            if (getOperation(operatorRecognizer, (String) textWithNumbers.get(0))
                    .equals("subtraction")) {
                firstNumber = firstNumber.multiply(-1);
            }
            i = 2;
        }

        result.inputInterpretation = new ArrayList<>();
        result.inputInterpretation.add(firstNumber);

        int currentVariableNumber = 0;
        final Map<String, Double> variables = new HashMap<>();
        variables.put("_" + currentVariableNumber,
                firstNumber.isDecimal() ? firstNumber.decimalValue() : firstNumber.integerValue());

        final StringBuilder expressionString = new StringBuilder();
        expressionString.append("_").append(currentVariableNumber);
        ++currentVariableNumber;

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
                    expressionString.append(
                            result.inputInterpretation.get(result.inputInterpretation.size() - 1));
                    number = number.lessThan(0) ? number.multiply(-1) : number;
                    break;
                case "subtraction":
                    result.inputInterpretation.add("-");
                    expressionString.append(
                            result.inputInterpretation.get(result.inputInterpretation.size() - 1));
                    break;
                case "multiplication":
                    result.inputInterpretation.add("x");
                    expressionString.append("*");
                    break;
                case "division":
                    result.inputInterpretation.add("÷");
                    expressionString.append("/");
                    break;
                case "power":
                    result.inputInterpretation.add("^");
                    expressionString.append(
                            result.inputInterpretation.get(result.inputInterpretation.size() - 1));
                    break;
            }

            variables.put("_" + currentVariableNumber,
                    number.isDecimal() ? number.decimalValue() : number.integerValue());
            expressionString.append("_").append(currentVariableNumber);
            ++currentVariableNumber;

            result.inputInterpretation.add(number);
        }

        result.number = new Number(new ExpressionBuilder(expressionString.toString())
                .variables(variables.keySet())
                .build()
                .setVariables(variables)
                .evaluate());

        operatorRecognizer.cleanup();
        return result;
    }
}
