package org.stypox.dicio.skills.calculator

import net.objecthunter.exp4j.ExpressionBuilder
import org.dicio.numbers.util.Number
import org.dicio.skill.chain.IntermediateProcessor
import org.dicio.skill.standard.StandardRecognizer
import org.dicio.skill.standard.StandardResult
import org.dicio.skill.util.WordExtractor
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated

class CalculatorProcessor : IntermediateProcessor<StandardResult, CalculatorOutput.Data?>() {
    private fun getOperation(
        operatorRecognizer: StandardRecognizer,
        text: String
    ): String {
        val inputWords: List<String> = WordExtractor.extractWords(text)
        val normalizedWordKeys: List<String> = WordExtractor.normalizeWords(inputWords)
        operatorRecognizer.setInput(text, inputWords, normalizedWordKeys)
        return if (operatorRecognizer.score() < 0.3) {
            "" // prevent bad matches
        } else {
            operatorRecognizer.result.sentenceId
        }
    }

    override fun process(data: StandardResult): CalculatorOutput.Data? {
        val textWithNumbers: List<Any> = ctx().numberParserFormatter!!
            .extractNumbers(data.getCapturingGroup("calculation")).get()
        if (textWithNumbers.isEmpty()
            || (textWithNumbers.size == 1 && textWithNumbers[0] !is Number)) {
            return null
        }

        val operatorRecognizer = StandardRecognizer(
            Sections.getSection(SectionsGenerated.calculator_operators)
        )
        var firstNumber: Number
        var i: Int
        if (textWithNumbers[0] is Number) {
            firstNumber = textWithNumbers[0] as Number
            i = 1
        } else {
            firstNumber = textWithNumbers[1] as Number
            if (getOperation(operatorRecognizer, textWithNumbers[0] as String)
                == "subtraction"
            ) {
                firstNumber = firstNumber.multiply(-1)
            }
            i = 2
        }

        val inputInterpretation = ArrayList<Any>()
        inputInterpretation.add(firstNumber)
        var currentVariableNumber = 0
        val variables: MutableMap<String, Double> = HashMap()
        variables["_0"] = if (firstNumber.isDecimal)
            firstNumber.decimalValue()
        else
            firstNumber.integerValue().toDouble()

        val expressionString = StringBuilder()
        expressionString.append("_").append(currentVariableNumber)
        ++currentVariableNumber
        while (i < textWithNumbers.size) {
            val operation: String
            if (textWithNumbers[i] is Number) {
                operation = "addition" // add two subsequent numbers
            } else if (i + 1 < textWithNumbers.size) {
                operation = getOperation(operatorRecognizer, textWithNumbers[i] as String)
                ++i
            } else {
                break
            }
            var number = textWithNumbers[i] as Number
            ++i
            when (operation) {
                "addition" -> {
                    inputInterpretation.add(if (number.lessThan(0)) "-" else "+")
                    expressionString.append(
                        inputInterpretation[inputInterpretation.size - 1]
                    )
                    number = if (number.lessThan(0)) number.multiply(-1) else number
                }
                "subtraction" -> {
                    inputInterpretation.add("-")
                    expressionString.append(
                        inputInterpretation[inputInterpretation.size - 1]
                    )
                }
                "multiplication" -> {
                    inputInterpretation.add("x")
                    expressionString.append("*")
                }
                "division" -> {
                    inputInterpretation.add("÷")
                    expressionString.append("/")
                }
                "power" -> {
                    inputInterpretation.add("^")
                    expressionString.append(
                        inputInterpretation[inputInterpretation.size - 1]
                    )
                }
                else -> {
                    inputInterpretation.add(if (number.lessThan(0)) "-" else "+")
                    expressionString.append(
                        inputInterpretation[inputInterpretation.size - 1]
                    )
                    number = if (number.lessThan(0)) number.multiply(-1) else number
                }
            }

            variables["_$currentVariableNumber"] = if (number.isDecimal)
                number.decimalValue()
            else
                number.integerValue().toDouble()

            expressionString.append("_").append(currentVariableNumber)
            ++currentVariableNumber
            inputInterpretation.add(number)
        }

        operatorRecognizer.cleanup()
        return CalculatorOutput.Data(
            inputInterpretation,
            Number(
                ExpressionBuilder(expressionString.toString())
                    .variables(variables.keys)
                    .build()
                    .setVariables(variables)
                    .evaluate()
            )
        )
    }
}
