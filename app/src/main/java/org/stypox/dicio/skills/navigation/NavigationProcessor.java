package org.stypox.dicio.skills.navigation;

import static org.stypox.dicio.Sentences_en.navigation;

import org.dicio.numbers.NumberParserFormatter;
import org.dicio.numbers.util.Number;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;

import java.util.List;

public class NavigationProcessor extends IntermediateProcessor<StandardResult, String> {
    @Override
    public String process(final StandardResult data) throws Exception {
        if (data == null) {
            return null;
        }

        final String placeToNavigate = data.getCapturingGroup(navigation.where);
        if (placeToNavigate == null) {
            return null;
        }

        final NumberParserFormatter npf = ctx().getNumberParserFormatter();
        if (npf == null) {
            // No number parser available, feed the spoken input directly to the map application.
            return placeToNavigate.trim();

        } else {
            final List<Object> textWithNumbers = ctx().requireNumberParserFormatter()
                    .extractNumbers(data.getCapturingGroup(navigation.where))
                    .preferOrdinal(true)
                    .get();

            // Given an address of "9546 19 avenue", the building number is 9546 and the street
            // number is 19.
            //
            // Known issues:
            // - Saying the building number using its digits one by one results in undesired spaces
            //   in between each one
            // - Saying the building number using partial grouping of its digits (but not all of
            //   them) e.g. "ninety five forty six" also results in undesired spaces in between each
            //   partial grouping
            //
            // Based on these known issues, for the example address given above, the speech provided
            // by the user should be "nine thousand five hundred forty six nineteen(th) avenue".

            final StringBuilder placeToNavigateSB = new StringBuilder();
            for (final Object currentItem : textWithNumbers) {
                if (currentItem instanceof String) {
                    placeToNavigateSB.append(currentItem);
                } else if (currentItem instanceof Number) {
                    final Number number = (Number) currentItem;
                    if (number.isInteger()) {
                        placeToNavigateSB.append(number.integerValue());
                    } else {
                        placeToNavigateSB.append(number.decimalValue());
                    }
                }
            }

            return placeToNavigateSB.toString().trim();
        }
    }
}
