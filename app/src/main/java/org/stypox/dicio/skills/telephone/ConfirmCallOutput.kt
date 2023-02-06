package org.stypox.dicio.skills.telephone

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import org.dicio.skill.SkillComponent
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.standard.StandardRecognizer
import org.dicio.skill.standard.StandardResult
import org.dicio.skill.util.NextSkills
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import org.stypox.dicio.output.graphical.GraphicalOutputUtils

class ConfirmCallOutput private constructor(private val number: String) :
    OutputGenerator<StandardResult>() {

    override fun generate(data: StandardResult) {
        val message: String
        if (data.sentenceId == "yes") {
            call(ctx().android(), number)
            message = ctx().android()
                .getString(R.string.skill_telephone_calling, number)
            // do not speak anything since a call has just started
        } else {
            message = ctx().android()
                .getString(R.string.skill_telephone_not_calling)
            ctx().speechOutputDevice.speak(message)
        }
        ctx().graphicalOutputDevice.display(
            GraphicalOutputUtils.buildSubHeader(ctx().android(), message)
        )
    }

    companion object {
        fun call(context: Context, number: String?) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$number")
            context.startActivity(callIntent)
        }

        /**
         * Writes output to the outputGenerator to ask for confirmation about calling the provided name,
         * and sets the outputGenerator's next skills to ensure that, if the user answers affirmatively,
         * the provided number is called.
         */
        fun <T> callAfterConfirmation(
            outputGenerator: T, name: String, number: String
        ) where T : SkillComponent, T : NextSkills {
            val context: Context = outputGenerator.ctx().android()
            val message = context.getString(R.string.skill_telephone_confirm_call, name)
            outputGenerator.ctx().speechOutputDevice.speak(message)
            val output: LinearLayout = GraphicalOutputUtils.buildVerticalLinearLayout(context, null)
            output.addView(GraphicalOutputUtils.buildSubHeader(context, message))
            output.addView(GraphicalOutputUtils.buildDescription(context, number))
            outputGenerator.ctx().graphicalOutputDevice.display(output)

            // ask for confirmation using the util_yes_no section
            outputGenerator.setNextSkills(
                listOf(
                    ChainSkill.Builder()
                        .recognize(StandardRecognizer(
                            Sections.getSection(SectionsGenerated.util_yes_no)))
                        .output(ConfirmCallOutput(number))
                )
            )
        }
    }
}