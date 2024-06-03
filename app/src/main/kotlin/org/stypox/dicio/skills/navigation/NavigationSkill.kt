package org.stypox.dicio.skills.navigation

import android.content.Intent
import android.net.Uri
import org.dicio.numbers.unit.Number
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Navigation
import java.util.Locale

class NavigationSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Navigation>)
    : StandardRecognizerSkill<Navigation>(correspondingSkillInfo, data) {
    override suspend fun generateOutput(ctx: SkillContext, scoreResult: Navigation): SkillOutput {
        val placeToNavigate: String = when (scoreResult) {
            is Navigation.Query -> scoreResult.where ?: return NavigationOutput(null)
        }

        val npf = ctx.parserFormatter
        val cleanPlaceToNavigate = if (npf == null) {
            // No number parser available, feed the spoken input directly to the map application.
            placeToNavigate.trim { it <= ' ' }
        } else {
            val textWithNumbers: List<Any> = npf
                .extractNumber(placeToNavigate)
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

        val uriGeoSimple = String.format(Locale.ENGLISH, "geo:0,0?q=%s", cleanPlaceToNavigate)
        val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriGeoSimple))
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ctx.android.startActivity(launchIntent)

        return NavigationOutput(cleanPlaceToNavigate)
    }
}
