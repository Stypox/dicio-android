package org.stypox.dicio.sentencesCompilerPlugin.data

import org.dicio.sentences_compiler.construct.SentenceConstructList
import java.io.File

data class ExtractedSkill(
    val id: String,
    val specificity: Specificity,
    val sentenceDefinitions: List<SentenceDefinition>,
    val languageToSentences: Map<String, List<RawSentence>>,
)

data class ParsedSkill(
    val id: String,
    val specificity: Specificity,
    val sentenceDefinitions: List<SentenceDefinition>,
    val languageToSentences: Map<String, List<ParsedSentence>>
)

data class RawSentence(
    val id: String,
    val file: File,
    val rawConstructs: String,
)

data class ParsedSentence(
    val id: String,
    val file: File,
    val constructs: SentenceConstructList,
)
