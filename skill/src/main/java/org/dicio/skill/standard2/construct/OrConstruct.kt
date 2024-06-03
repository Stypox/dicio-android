package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.normalizeMemToEnd

data class OrConstruct(
    private val constructs: List<Construct>
) : Construct {
    private fun evaluateConstruct(
        construct: Construct,
        immutableMemToEnd: Array<StandardMatchResult>,
        helper: MatchHelper,
    ): Array<StandardMatchResult> {
        val newMemToEnd: Array<StandardMatchResult> = immutableMemToEnd.clone()
        construct.matchToEnd(newMemToEnd, helper)
        return newMemToEnd
    }

    override fun matchToEnd(memToEnd: Array<StandardMatchResult>, helper: MatchHelper) {
        if (constructs.isEmpty()) {
            // edge case that doesn't make sense, just treat this as an OptionalConstruct
            return
        }

        // memToEnd remains immutable during this loop, and gets cloned when passed to functions
        val bestNewMemToEnd = evaluateConstruct(constructs[0], memToEnd, helper)
        for (j in 1..<constructs.size) {
            val newMemToEnd = evaluateConstruct(constructs[j], memToEnd, helper)
            for (start in bestNewMemToEnd.indices) {
                bestNewMemToEnd[start] = StandardMatchResult.keepBest(
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
