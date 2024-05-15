package org.stypox.dicio.io.graphical

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.dicio.skill.SkillContext
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.util.getString

class ErrorSkillOutput(
    private val throwable: Throwable
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String =
        ctx.getString(R.string.eval_fatal_error)

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        // TODO
        Text(text = throwable.message ?: throwable::class.simpleName ?: "")
    }
}
