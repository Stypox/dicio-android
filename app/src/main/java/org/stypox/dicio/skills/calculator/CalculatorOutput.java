package org.stypox.dicio.skills.calculator;

import androidx.core.content.res.ResourcesCompat;

import org.stypox.dicio.R;
import org.stypox.dicio.output.graphical.GraphicalOutputUtils;
import org.dicio.numbers.util.Number;
import org.dicio.skill.chain.OutputGenerator;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class CalculatorOutput extends OutputGenerator<CalculatorOutput.Data> {

    public static class Data {
        boolean failed = false;
        List<Object> inputInterpretation;
        Number number;
    }


    private String numberToString(final DecimalFormat decimalFormat, final Number number) {
        if (number.isDecimal()) {
            return decimalFormat.format(number.decimalValue());
        } else {
            return String.valueOf(number.integerValue());
        }
    }

    @Override
    public void generate(final Data data) {
        if (data.failed) {
            final String message = ctx().android().getString(
                    R.string.skill_calculator_could_not_calculate);
            ctx().getSpeechOutputDevice().speak(message);
            ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildHeader(
                    ctx().android(), message));

        } else {
            final DecimalFormat decimalFormat
                    = new DecimalFormat("#.##", new DecimalFormatSymbols(ctx().getLocale()));

            final StringBuilder inputInterpretation = new StringBuilder();
            for (int i = 0; i < data.inputInterpretation.size(); ++i) {
                if (data.inputInterpretation.get(i) instanceof Number) {
                    inputInterpretation.append(numberToString(decimalFormat,
                            (Number) data.inputInterpretation.get(i)));
                } else {
                    inputInterpretation.append(data.inputInterpretation.get(i).toString());
                }
                inputInterpretation.append(" ");
            }
            inputInterpretation.append("=");

            ctx().getSpeechOutputDevice().speak(ctx().requireNumberParserFormatter()
                    .niceNumber(data.number.isDecimal()
                            ? data.number.decimalValue() : data.number.integerValue())
                    .get());
            ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildVerticalLinearLayout(
                    ctx().android(),
                    ResourcesCompat.getDrawable(ctx().android().getResources(),
                            R.drawable.divider_items, null),
                    GraphicalOutputUtils.buildDescription(
                            ctx().android(), inputInterpretation.toString()),
                    GraphicalOutputUtils.buildHeader(
                            ctx().android(),
                            numberToString(decimalFormat, data.number))));
        }
    }
}
