package org.stypox.dicio.skills.navigation

import org.dicio.numbers.unit.Number
import org.dicio.skill.chain.IntermediateProcessor
import org.dicio.skill.standard.StandardResult
import org.stypox.dicio.Sentences_en.navigation

class NavigationProcessor : IntermediateProcessor<StandardResult, String?>() {
    @Throws(Exception::class)
    override fun process(data: StandardResult): String? {
        val placeToNavigate: String = data.getCapturingGroup(navigation.where) ?: return null
        val npf = ctx().parserFormatter
        return if (npf == null) {
            // No number parser available, feed the spoken input directly to the map application.
            placeToNavigate.trim { it <= ' ' }
        } else {
            val textWithNumbers: List<Any> = npf
                .extractNumber(data.getCapturingGroup(navigation.where))
                .preferOrdinal(true)
                .mixedWithText

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
            val placeToNavigateSB = StringBuilder()
            for (currentItem in textWithNumbers) {
                if (currentItem is String) {
                    placeToNavigateSB.append(currentItem)
                } else if (currentItem is Number) {
                    if (currentItem.isInteger) {
                        placeToNavigateSB.append(currentItem.integerValue())
                    } else {
                        placeToNavigateSB.append(currentItem.decimalValue())
                    }
                }
            }
            placeToNavigateSB.toString().trim { it <= ' ' }
        }
    }
}
