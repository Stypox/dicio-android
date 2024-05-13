package org.stypox.dicio.skills.telephone

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.standard.StandardResult

class ConfirmCallGenerator(private val number: String) :
    OutputGenerator<StandardResult>() {

    override fun generate(data: StandardResult): SkillOutput {
        return if (data.sentenceId == "yes") {
            call(ctx().android!!, number)
            ConfirmedCallOutput(ctx().android!!, number)
        } else {
            ConfirmedCallOutput(ctx().android!!, null)
        }
    }

    companion object {
        fun call(context: Context, number: String?) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$number")
            context.startActivity(callIntent)
        }
    }
}
