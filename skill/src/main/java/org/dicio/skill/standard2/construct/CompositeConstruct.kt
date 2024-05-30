package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.cumulativeWeight

data class CompositeConstruct(
    private val constructs: List<Construct>
) : Construct {
    override fun match(start: Int, end: Int, ctx: MatchHelper): StandardMatchResult {
        val cumulativeWeight = ctx.getOrTokenize("cumulativeWeight", ::cumulativeWeight)
        val mem: Array<Array<StandardMatchResult?>> =
            Array(end-start+1) { Array(constructs.size) { null } }

        fun dp(compStart: Int, j: Int): StandardMatchResult {
            if (j >= constructs.size) {
                return StandardMatchResult.empty(compStart, false)
            }
            mem[compStart - start][j]?.let { return it }

            val result = (compStart..end)
                .map { compEnd ->
                    val compResult = constructs[j].match(compStart, compEnd, ctx)
                    val dpResult = dp(compEnd, j+1)
                    val skippedWordsWeight =
                        cumulativeWeight[compEnd] - cumulativeWeight[compResult.end]

                    return@map StandardMatchResult(
                        userMatched = compResult.userMatched + dpResult.userMatched,
                        userWeight = compResult.userWeight + skippedWordsWeight +
                                dpResult.userWeight,
                        refMatched = compResult.refMatched + dpResult.refMatched,
                        refWeight = compResult.refWeight + dpResult.refWeight,
                        end = dpResult.end,
                        canGrow = compResult.canGrow || dpResult.canGrow,
                        capturingGroups = if (compResult.capturingGroups == null) {
                            dpResult.capturingGroups
                        } else if (dpResult.capturingGroups == null) {
                            compResult.capturingGroups
                        } else {
                            dpResult.capturingGroups + compResult.capturingGroups
                        }
                    )
                }
                // it is impossible for the result to be null because the (compStart..end) range
                // is always non-empty (even if compStart == end), hence the !!
                .fold(null, StandardMatchResult::keepBest)!!

            mem[compStart - start][j] = result
            return result
        }

        return dp(start, 0)
    }
}
