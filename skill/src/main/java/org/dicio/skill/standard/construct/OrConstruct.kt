package org.dicio.skill.standard.construct

import org.dicio.skill.standard.StandardScore
import org.dicio.skill.standard.helper.MatchHelper
import org.dicio.skill.standard.helper.normalizeMemToEnd

data class OrConstruct(
    private val constructs: List<Construct>
) : Construct {
    private fun evaluateConstruct(
        construct: Construct,
        immutableMemToEnd: Array<StandardScore>,
        helper: MatchHelper,
    ): Array<StandardScore> {
        val newMemToEnd: Array<StandardScore> = immutableMemToEnd.clone()
        construct.matchToEnd(newMemToEnd, helper)
        return newMemToEnd
    }

    override fun matchToEnd(memToEnd: Array<StandardScore>, helper: MatchHelper) {
        if (constructs.isEmpty()) {
            // edge case that doesn't make sense, just treat this as an OptionalConstruct
            return
        }

        // memToEnd remains immutable during this loop, and gets cloned when passed to functions
        val bestNewMemToEnd = evaluateConstruct(constructs[0], memToEnd, helper)
        for (j in 1..<constructs.size) {
            val newMemToEnd = evaluateConstruct(constructs[j], memToEnd, helper)
            for (start in bestNewMemToEnd.indices) {
                bestNewMemToEnd[start] = StandardScore.keepBest(
                    bestNewMemToEnd[start],
                    newMemToEnd[start],
                )
            }
        }

        // memToEnd is mutated only here
        for (start in bestNewMemToEnd.indices) {
            memToEnd[start] = bestNewMemToEnd[start]
        }

        normalizeMemToEnd(memToEnd, helper.cumulativeWeight)
    }

    override fun toString(): String {
        return constructs.joinToString("|") { it.toString() }
    }
}
