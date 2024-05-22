package org.stypox.dicio.skills.calculator

import net.objecthunter.exp4j.ExpressionBuilder
import org.dicio.numbers.unit.Number
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.dicio.skill.standard.StandardResult
import org.dicio.skill.util.WordExtractor
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CalculatorSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData)
    : StandardRecognizerSkill(correspondingSkillInfo, data) {

    private fun getOperation(
        operatorSection: StandardRecognizerData,
        text: String
    ): String {
        val inputWords: List<String> = WordExtractor.extractWords(text)
        val normalizedWordKeys: List<String> = WordExtractor.normalizeWords(inputWords)
        val (score, result) = operatorSection.score(text, inputWords, normalizedWordKeys)
        return if (score < 0.3) {
            "" // prevent bad matches
        } else {
            result.sentenceId
        }
    }

    private fun numberToString(decimalFormat: DecimalFormat, number: Number): String {
        return if (number.isDecimal) {
            decimalFormat.format(number.decimalValue())
        } else {
            number.integerValue().toString()
        }
    }

    override suspend fun generateOutput(ctx: SkillContext, scoreResult: StandardResult): SkillOutput {
        val textWithNumbers: List<Any>? = scoreResult.getCapturingGroup("calculation")
            ?.let { ctx.parserFormatter?.extractNumber(it)?.mixedWithText }
        if (textWithNumbers.isNullOrEmpty()
            || (textWithNumbers.size == 1 && textWithNumbers[0] !is Number)) {
            return CalculatorOutput(null, "", "")
        }

        val operatorSection = Sections.getSection(SectionsGenerated.calculator_operators)
        var firstNumber: Number
        var i: Int
        if (textWithNumbers[0] is Number) {
            firstNumber = textWithNumbers[0] as Number
            i = 1
        } else {
            firstNumber = textWithNumbers[1] as Number
            if (getOperation(operatorSection, textWithNumbers[0] as String)
                == "subtraction"
            ) {
                firstNumber = firstNumber.multiply(-1)
            }
            i = 2
        }

        val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(ctx.locale))
        val inputInterpretation = StringBuilder(numberToString(decimalFormat, firstNumber))

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
                operation = getOperation(operatorSection, textWithNumbers[i] as String)
                ++i
            } else {
                break
            }
            var number = textWithNumbers[i] as Number
            ++i
            when (operation) {
                "subtraction" -> {
                    inputInterpretation.append(" - ")
                    expressionString.append("-")
                }
                "multiplication" -> {
                    inputInterpretation.append(" x ")
                    expressionString.append("*")
                }
                "division" -> {
                    inputInterpretation.append(" ÷ ")
                    expressionString.append("/")
                }
                "power" -> {
                    inputInterpretation.append(" ^ ")
                    expressionString.append("^")
                }
                else -> { // "addition"
                    val op = if (number.lessThan(0)) "-" else "+"
                    inputInterpretation.append(" $op ")
                    expressionString.append(op)
                    number = if (number.lessThan(0)) number.multiply(-1) else number
                }
            }

            variables["_$currentVariableNumber"] = if (number.isDecimal)
                number.decimalValue()
            else
                number.integerValue().toDouble()

            expressionString.append("_").append(currentVariableNumber)
            ++currentVariableNumber
            inputInterpretation.append(numberToString(decimalFormat, number))
        }
        inputInterpretation.append(" =")

        val result = ExpressionBuilder(expressionString.toString())
            .variables(variables.keys)
            .build()
            .setVariables(variables)
            .evaluate()

        return CalculatorOutput(
            result = numberToString(decimalFormat, Number(result)),
            spokenResult = ctx.parserFormatter!!
                .niceNumber(result)
                .get(),
            inputInterpretation = inputInterpretation.toString(),
        )
    }
}
