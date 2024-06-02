package org.dicio.skill.standard2.helper

import org.dicio.skill.standard2.StandardMatchResult


fun initialMemToEnd(cumulativeWeight: FloatArray): Array<StandardMatchResult> {
    val endWeight = cumulativeWeight.last()
    return Array(cumulativeWeight.size) { start ->
        StandardMatchResult(
            userMatched = 0.0f,
            userWeight = endWeight - cumulativeWeight[start],
            refMatched = 0.0f,
            refWeight = 0.0f,
            capturingGroups = null,
        )
    }
}

fun normalizeMemToEnd(memToEnd: Array<StandardMatchResult>, cumulativeWeight: FloatArray) {
    for (i in memToEnd.size-2 downTo 0) {
        memToEnd[i] = StandardMatchResult.keepBest(
            memToEnd[i],
            memToEnd[i+1].plus(userWeight = cumulativeWeight[i+1] - cumulativeWeight[i]),
        )
    }
}
