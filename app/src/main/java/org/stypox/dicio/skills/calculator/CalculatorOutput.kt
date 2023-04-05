package org.stypox.dicio.skills.calculator

import androidx.core.content.res.ResourcesCompat
import org.dicio.numbers.util.Number
import org.dicio.skill.chain.OutputGenerator
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.GraphicalOutputUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CalculatorOutput : OutputGenerator<CalculatorOutput.Data?>() {
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

    override fun generate(data: Data?) {
        if (data == null) {
            val message = ctx().android().getString(
                R.string.skill_calculator_could_not_calculate
            )
            ctx().speechOutputDevice.speak(message)
            ctx().graphicalOutputDevice.display(
                GraphicalOutputUtils.buildHeader(
                    ctx().android(), message
                )
            )
        } else {
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
            ctx().speechOutputDevice.speak(
                ctx().requireNumberParserFormatter()
                    .niceNumber(
                        if (data.number.isDecimal)
                            data.number.decimalValue()
                        else
                            data.number.integerValue().toDouble()
                    )
                    .get()
            )
            ctx().graphicalOutputDevice.display(
                GraphicalOutputUtils.buildVerticalLinearLayout(
                    ctx().android(),
                    ResourcesCompat.getDrawable(
                        ctx().android().resources,
                        R.drawable.divider_items, null
                    ),
                    GraphicalOutputUtils.buildDescription(
                        ctx().android(), inputInterpretation.toString()
                    ),
                    GraphicalOutputUtils.buildHeader(
                        ctx().android(),
                        numberToString(decimalFormat, data.number)
                    )
                )
            )
        }
    }
}