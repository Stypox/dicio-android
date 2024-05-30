package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.cumulativeWeight

data class CompositeConstruct(
    private val constructs: List<Construct>
) : Construct {
    private var mem: Array<Array<MutableList<StandardMatchResult?>>> = arrayOf()

    override fun match(start: Int, end: Int, helper: MatchHelper): StandardMatchResult {
        val cumulativeWeight = helper.getOrTokenize("cumulativeWeight", ::cumulativeWeight)

        fun dp(compStart: Int, j: Int): StandardMatchResult {
            if (j >= constructs.size) {
                return StandardMatchResult.empty(compStart, false)
            }
            val memAtStart = mem[j][compStart]
            if (memAtStart.size <= end - compStart) {
                return memAtStart.last()!!
            }
            memAtStart[end - compStart]?.let { return it }

            val result = (compStart..end)
                .map { compEnd ->
                    val compResult = constructs[j].match(compStart, compEnd, helper)
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

            mem[j][compStart][end - compStart] = result
            if (!result.canGrow) {
                for (i in end - compStart + 1..<mem[j][compStart].size) {
                    mem[j][compStart].removeLast()
                }
            }
            return result
        }

        return dp(start, 0)
    }

    override fun setupCache(helper: MatchHelper) {
        mem = Array(constructs.size) {
            Array(helper.userInput.length + 1) { i ->
                MutableList(helper.userInput.length + 1 - i) { null }
            }
        }
        constructs.forEach { it.setupCache(helper) }
    }

    override fun destroyCache() {
        mem = arrayOf()
        constructs.forEach { it.destroyCache() }
    }
}
