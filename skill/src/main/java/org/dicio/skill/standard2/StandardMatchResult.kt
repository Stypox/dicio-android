package org.dicio.skill.standard2

import org.dicio.skill.standard2.capture.Capture
import org.dicio.skill.standard2.capture.NamedCapture
import org.dicio.skill.standard2.capture.StringRangeCapture

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

    // will form a binary tree structure, where inner nodes are Pairs of subtrees,
    // while the leaves are either Capture or StringRangeCapture
    val capturingGroups: Any?,
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
        // TODO choose better expression
        return 0.5f * (
                (if (userWeight > 0.0f) userMatched / userWeight else 0.0f) +
                (if (refWeight > 0.0f) refMatched / refWeight else 0.0f)
        )
    }

    fun exploreCapturingGroupsTree(node: Any?, name: String): NamedCapture? {
        return when (node) {
            null ->
                null

            is Pair<*, *> ->
                exploreCapturingGroupsTree(node.first, name)
                    ?: exploreCapturingGroupsTree(node.second, name)

            is NamedCapture ->
                if (node.name == name) node else null

            else ->
                throw IllegalArgumentException(
                    "Unexpected type found in capturing groups tree: type=${
                        node::class.simpleName
                    }, value=$node"
                )
        }
    }

    inline fun <reified T> getCapturingGroup(userInput: String, name: String): T? {
        val result = exploreCapturingGroupsTree(capturingGroups, name) ?: return null
        return when {
            result is Capture && result.value is T ->
                result.value

            result is StringRangeCapture && T::class == String::class ->
                userInput.subSequence(result.start, result.end) as T

            else ->
                throw IllegalArgumentException("Capturing group \"$name\" has wrong type: expectedType=${
                    T::class.simpleName}, actualType=${result::class.simpleName}, actualValue=\"$result\"")
        }
    }

    companion object {
        fun empty(end: Int, canGrow: Boolean): StandardMatchResult {
            return StandardMatchResult(0.0f, 0.0f, 0.0f, 0.0f, end, canGrow, null)
        }

        fun keepBest(m1: StandardMatchResult?, m2: StandardMatchResult): StandardMatchResult {
            return if (m1 == null || m2.score() > m1.score()) m2 else m1
        }

        const val UM: Float = 2.0f;
        const val UW: Float = -1.1f;
        const val RM: Float = 2.0f;
        const val RW: Float = -1.1f;
    }
}
