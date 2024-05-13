package org.stypox.dicio.skills.calculator

import org.dicio.numbers.unit.Number
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CalculatorGenerator : OutputGenerator<CalculatorGenerator.Data?>() {
    data class Data(
        val inputInterpretation: MutableList<Any>,
        val number: Number
    )

    private fun numberToString(decimalFormat: DecimalFormat, number: Number): String {
        return if (number.isDecimal) {
            decimalFormat.format(number.decimalValue())
        } else {
            number.integerValue().toString()
        }
    }

    override fun generate(data: Data?): SkillOutput {
        if (data == null) {
            return CalculatorOutput(ctx().android!!, null, "", "")
        }

        val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(ctx().locale))
        val inputInterpretation = StringBuilder()
        for (i in data.inputInterpretation.indices) {
            if (data.inputInterpretation[i] is Number) {
                inputInterpretation.append(
                    numberToString(
                        decimalFormat,
                        data.inputInterpretation[i] as Number
                    )
                )
            } else {
                inputInterpretation.append(data.inputInterpretation[i].toString())
            }
            inputInterpretation.append(" ")
        }
        inputInterpretation.append("=")

        return CalculatorOutput(
            context = ctx().android!!,
            result = numberToString(decimalFormat, data.number),
            spokenResult = ctx().parserFormatter!!
                .niceNumber(
                    if (data.number.isDecimal)
                        data.number.decimalValue()
                    else
                        data.number.integerValue().toDouble()
                )
                .get(),
            inputInterpretation = inputInterpretation.toString(),
        )
    }
}
