package org.dicio.skill.skill

interface Score {
    fun scoreIn01Range(): Float
    fun isBetterThan(other: Score): Boolean
}

object AlwaysBestScore : Score {
    override fun scoreIn01Range() = 1.0f
    override fun isBetterThan(other: Score) = true
}

object AlwaysWorstScore : Score {
    override fun scoreIn01Range() = 0.0f
    override fun isBetterThan(other: Score) = false
}

data class FloatScore(val score: Float) : Score {
    override fun scoreIn01Range() = score
    override fun isBetterThan(other: Score) = score > other.scoreIn01Range()
}
