package org.dicio.skill.standard2

data class StandardMatchResult(
    val userMatched: Float,
    val userWeight: Float,
    val refMatched: Float,
    val refWeight: Float,

    /**
     * Exclusive index
     */
    val end: Int,
    val canGrow: Boolean,

    val capturingGroups: Map<String, Pair<Int, Int>>,
) {
    fun score(): Float {
        return UM * userMatched +
                UW * userWeight +
                RM * refMatched +
                RW * refWeight
    }

    /**
     * This is not a well-behaving score and **should not** be used to compare two
     * [StandardMatchResult]s. This is to be used strictly only when a score in range
     * [[0, 1]] is needed, e.g. to compare with scores of other types.
     */
    fun scoreIn01Range(): Float {
        return (userMatched + refMatched) / (userWeight + refWeight)
    }

    companion object {
        fun empty(end: Int, canGrow: Boolean): StandardMatchResult {
            return StandardMatchResult(0.0f, 0.0f, 0.0f, 0.0f, end, canGrow, mapOf())
        }

        fun keepBest(m1: StandardMatchResult?, m2: StandardMatchResult): StandardMatchResult {
            return if (m1 == null || m2.score() > m1.score()) m2 else m1
        }

        fun keepBest(m1: StandardMatchResult, m2: StandardMatchResult): StandardMatchResult {
            return if (m2.score() > m1.score()) m2 else m1
        }

        const val UM: Float = 2.0f;
        const val UW: Float = -1.1f;
        const val RM: Float = 2.0f;
        const val RW: Float = -1.1f;
    }
}
