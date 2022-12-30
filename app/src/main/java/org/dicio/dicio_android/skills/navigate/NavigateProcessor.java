package org.dicio.dicio_android.skills.navigate;

import static org.dicio.dicio_android.Sentences_en.navigate;

import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;

import java.util.List;

public class NavigateProcessor extends IntermediateProcessor<StandardResult, NavigateOutput.Data> {
    @Override
    public NavigateOutput.Data process(final StandardResult data) throws Exception {
        final NavigateOutput.Data result = new NavigateOutput.Data();

        if (data == null) {
            result.failed = true;
            return result;
        }

        String placeToNavigate = data.getCapturingGroup(navigate.where);

        if (placeToNavigate == null) {
            result.failed = true;
            return result;
        }

        placeToNavigate = placeToNavigate.trim();

        if (StringUtils.isNullOrEmpty(placeToNavigate)) {
            result.failed = true;
            return result;
        }

        final List<Object> textWithNumbers = ctx().requireNumberParserFormatter()
                .extractNumbers(data.getCapturingGroup(navigate.where)).get();

        // It seems that we detect each digit of the numerical portion of an address as a separate
        // string, resulting in "9 5 4 6 1 9 avenue" when the speech was actually "9546 19 avenue".
        // Here we will remove the spaces from between the numbers, so we will end up with
        // "954619 avenue". This is not ideal, but it seems slightly better than before.
        // Can we detect addresses better?
        final StringBuilder placeToNavigateSB = new StringBuilder();
        int i = 0;
        while (i < textWithNumbers.size()) {
            final Object currentItem = textWithNumbers.get(i);
            if (!currentItem.toString().equals(" ")) {
                if (i == 0 || isInteger(currentItem.toString())) {
                    placeToNavigateSB.append(currentItem.toString().trim());
                } else {
                    placeToNavigateSB.append(" ").append(currentItem).append(" ");
                }
            }

            i++;
        }

        result.failed = false;
        result.address = placeToNavigateSB.toString();
        return result;
    }

    public static boolean isInteger(final String string) {
        System.out.printf("Parsing string: \"%s\"%n", string);

        if (string == null || string.equals("")) {
            System.out.println("String cannot be parsed, it is null or empty.");
            return false;
        }

        try {
            final int intValue = Integer.parseInt(string);
            return true;
        } catch (final NumberFormatException e) {
            System.out.println("Input String cannot be parsed to Integer.");
        }
        return false;
    }
}
