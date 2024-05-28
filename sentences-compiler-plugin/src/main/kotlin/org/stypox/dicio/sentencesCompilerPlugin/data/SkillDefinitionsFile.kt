package org.stypox.dicio.sentencesCompilerPlugin.data

import com.fasterxml.jackson.annotation.JsonValue

data class SkillDefinitionsFile(
    val skills: List<SkillDefinition>
)

data class SkillDefinition(
    val id: String,
    val specificity: Specificity,
    val sentences: List<SentenceDefinition>
)

enum class Specificity(@JsonValue val yamlValue: String) {
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low"),
}

data class SentenceDefinition(
    val id: String,
    val captures: List<CaptureDefinition>
)

data class CaptureDefinition(
    val id: String,
    val type: CaptureType
)

enum class CaptureType(@JsonValue val yamlValue: String) {
    STRING("string"),
    DURATION("duration")
}
