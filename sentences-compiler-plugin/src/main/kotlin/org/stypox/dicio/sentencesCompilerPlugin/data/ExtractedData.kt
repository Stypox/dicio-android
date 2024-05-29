package org.stypox.dicio.sentencesCompilerPlugin.data

import org.dicio.sentences_compiler.construct.SentenceConstructList

data class RawExtractedData(
    val skills: List<SkillDefinition>,
    val languageToSentences: Map<String, List<RawSentence>>
)

data class ParsedExtractedData(
    val skills: List<SkillDefinition>,
    val languageToSentences: Map<String, List<ParsedSentence>>
)

data class RawSentence(
    val id: String,
    val rawConstructs: String,
)

data class ParsedSentence(
    val id: String,
    val constructs: SentenceConstructList,
)
