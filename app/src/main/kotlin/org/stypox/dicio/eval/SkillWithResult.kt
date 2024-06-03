package org.stypox.dicio.eval

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillOutput

data class SkillWithResult<ScoreResult>(
    val skill: Skill<ScoreResult>,
    val score: Float,
    val scoreResult: ScoreResult,
) {
    suspend fun generateOutput(ctx: SkillContext): SkillOutput {
        return skill.generateOutput(ctx, scoreResult)
    }
}

fun <ScoreResult> Skill<ScoreResult>.scoreAndWrapResult(
    ctx: SkillContext,
    input: String,
): SkillWithResult<ScoreResult> {
    val (score, result) = score(ctx, input)
    return SkillWithResult(
        skill = this,
        score = score,
        scoreResult = result,
    )
}
