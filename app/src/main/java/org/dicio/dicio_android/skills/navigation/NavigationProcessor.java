package org.dicio.dicio_android.skills.navigation;

import static org.dicio.dicio_android.Sentences_en.navigation;

import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;

import java.util.List;

public class NavigationProcessor
        extends IntermediateProcessor<StandardResult, NavigationOutput.Data> {
    @Override
    public NavigationOutput.Data process(final StandardResult data) throws Exception {
        final NavigationOutput.Data result = new NavigationOutput.Data();

        if (data == null) {
            result.failed = true;
            return result;
        }

        String placeToNavigate = data.getCapturingGroup(navigation.where);

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
                .extractNumbers(data.getCapturingGroup(navigation.where)).get();

        // Given an address of "9546 19 avenue", the building number is 9546 and the street number
        // is 19.
        //
        // Known issues:
        // - Specifying the street number as an ordinal seems to result in some undesired
        // multiplication happening
        // - Saying the building number using its digits one by one results in undesired spaces in
        // between each one
        // - Saying the building number using partial grouping of its digits (but not all of them)
        // e.g. "ninety five forty six" also results in undesired spaces in between each partial
        // grouping
        //
        // Based on these known issues, for the example address given above, the speech provided by
        // the user should be "nine thousand five hundred forty six nineteen avenue".

        final StringBuilder placeToNavigateSB = new StringBuilder();
        for (final Object currentItem : textWithNumbers) {
            placeToNavigateSB.append(currentItem.toString());
        }

        result.failed = false;
        result.address = placeToNavigateSB.toString().trim();
        return result;
    }
}
