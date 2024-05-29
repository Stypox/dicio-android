package org.stypox.dicio.sentencesCompilerPlugin.data

import org.gradle.api.logging.Logger
import org.stypox.dicio.sentencesCompilerPlugin.util.SKILL_DEFINITIONS_FILE
import org.stypox.dicio.sentencesCompilerPlugin.util.SentencesCompilerPluginException
import org.stypox.dicio.sentencesCompilerPlugin.util.YML_EXT
import java.io.File

fun extractDataFromFiles(logger: Logger, inputDirFile: File): RawExtractedData {
    val definitions: SkillDefinitionsFile =
        parseYamlFile(File(inputDirFile, SKILL_DEFINITIONS_FILE))
    val languageToSentences = HashMap<String, List<RawSentence>>()

    for (lang in inputDirFile.listFiles { file -> file.isDirectory }!!) {
        val sentences = ArrayList<RawSentence>()
        for (skill in definitions.skills) {
            val file = File(lang, skill.id + YML_EXT)
            if (!file.exists()) {
                continue
            }

            val parsedSentences: Map<String, List<String>?> = parseYamlFile(file)
            val expectedSentenceIds = skill.sentences.map { it.id }.toSet()
            if (!parsedSentences.keys.containsAll(expectedSentenceIds)) {
                throw SentencesCompilerPluginException(
                    "Skill sentences file ${lang.name}/${
                        file.name
                    } is missing these sentence ids ${
                        expectedSentenceIds - parsedSentences.keys
                    }: ${file.absolutePath}"
                )
            } else if (!expectedSentenceIds.containsAll(parsedSentences.keys)) {
                throw SentencesCompilerPluginException(
                    "Skill sentences file ${lang.name}/${
                        file.name
                    } has these superfluous sentence ids ${
                        parsedSentences.keys - expectedSentenceIds
                    }: ${file.absolutePath}"
                )
            }

            val emptySentences = parsedSentences
                .filter { it.value.isNullOrEmpty() }
                .map { it.key }
            if (emptySentences.isNotEmpty()) {
                logger.error(
                    "[Warning] Skill sentences file ${lang.name}/${
                        file.name
                    } has no sentence definitions for these sentence ids ${
                        emptySentences
                    }: ${file.absolutePath}"
                )
            }

            for ((sentenceId, parsedSentencesWithoutId) in parsedSentences) {
                if (parsedSentencesWithoutId == null) continue
                for (sentence in parsedSentencesWithoutId) {
                    sentences.add(RawSentence(id = sentenceId, rawConstructs = sentence))
                }
            }
        }

        if (sentences.isNotEmpty()) {
            languageToSentences[lang.name] = sentences
        }

        // issue a warning for unknown files
        for (file in lang.listFiles()!!) {
            if (definitions.skills.all { it.id + YML_EXT != file.name }) {
                logger.error(
                    "[Warning] Skill sentences file ${lang.name}/${
                        file.name
                    } does not correspond to any skill: ${file.absolutePath}"
                )
            }
        }
    }

    return RawExtractedData(
        skills = definitions.skills,
        languageToSentences = languageToSentences,
    )
}
