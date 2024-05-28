package org.dicio.skill.standard

import org.dicio.skill.util.WordExtractor

open class StandardResult(
    val sentenceId: String,
    private val input: String,
    val capturingGroupRanges: Map<String, InputWordRange>
) {
    open fun getCapturingGroup(name: String): String? {
        return capturingGroupRanges.getOrDefault(name, null)?.let {
            WordExtractor.extractCapturingGroup(input, it)
        }
    }
}
