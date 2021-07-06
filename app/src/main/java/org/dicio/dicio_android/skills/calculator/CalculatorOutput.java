package org.dicio.dicio_android.skills.calculator;

import androidx.core.content.res.ResourcesCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.numbers.util.Number;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class CalculatorOutput implements OutputGenerator<CalculatorOutput.Data> {

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
    public void generate(final Data data,
                         final SkillContext context,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {
        if (data.failed) {
            final String message = context.getAndroidContext().getString(
                    R.string.skill_calculator_could_not_calculate);
            speechOutputDevice.speak(message);
            graphicalOutputDevice.display(GraphicalOutputUtils.buildHeader(
                    context.getAndroidContext(), message));

        } else {
            final DecimalFormat decimalFormat
                    = new DecimalFormat("#.##", new DecimalFormatSymbols(context.getLocale()));

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

            graphicalOutputDevice.display(GraphicalOutputUtils.buildVerticalLinearLayout(
                    context.getAndroidContext(),
                    ResourcesCompat.getDrawable(context.getAndroidContext().getResources(),
                            R.drawable.divider_items, null),
                    GraphicalOutputUtils.buildDescription(
                            context.getAndroidContext(), inputInterpretation.toString()),
                    GraphicalOutputUtils.buildHeader(
                            context.getAndroidContext(),
                            numberToString(decimalFormat, data.number))));
        }
    }

    @Override
    public void cleanup() {
    }
}
