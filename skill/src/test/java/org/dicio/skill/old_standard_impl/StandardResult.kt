package org.dicio.skill.old_standard_impl

import org.dicio.skill.old_standard.WordExtractor

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
