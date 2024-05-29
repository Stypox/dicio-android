package org.stypox.dicio.sentencesCompilerPlugin.data

import org.gradle.api.logging.Logger
import org.stypox.dicio.sentencesCompilerPlugin.util.SKILL_DEFINITIONS_FILE
import org.stypox.dicio.sentencesCompilerPlugin.util.SentencesCompilerPluginException
import org.stypox.dicio.sentencesCompilerPlugin.util.YML_EXT
import java.io.File

fun readDataFromFiles(logger: Logger, inputDirFile: File): SkillDefinitionsFile {
    val definitions: SkillDefinitionsFile =
        parseYamlFile(File(inputDirFile, SKILL_DEFINITIONS_FILE))
    val languages = ArrayList<String>()

    for (lang in inputDirFile.listFiles { file -> file.isDirectory }!!) {
        var langHasSkill = false
        for (skill in definitions.skills) {
            val file = File(lang, skill.id + YML_EXT)
            if (!file.exists()) {
                continue
            }
            langHasSkill = true

            val sentences: Map<String, List<String>?> = parseYamlFile(file)
            val expectedSentenceIds = skill.sentences.map { it.id }.toSet()
            if (!sentences.keys.containsAll(expectedSentenceIds)) {
                throw SentencesCompilerPluginException(
                    "Skill sentences file ${lang.name}/${
                        file.name
                    } is missing these sentence ids ${
                        expectedSentenceIds - sentences.keys
                    }: ${file.absolutePath}"
                )
            } else if (!expectedSentenceIds.containsAll(sentences.keys)) {
                throw SentencesCompilerPluginException(
                    "Skill sentences file ${lang.name}/${
                        file.name
                    } has these superfluous sentence ids ${
                        sentences.keys - expectedSentenceIds
                    }: ${file.absolutePath}"
                )
            }

            val emptySentences = sentences.filter { it.value.isNullOrEmpty() }.map { it.key }
            if (emptySentences.isNotEmpty()) {
                throw SentencesCompilerPluginException(
                    "Skill sentences file ${lang.name}/${
                        file.name
                    } has no sentence definitions for these sentence ids ${
                        emptySentences
                    }: ${file.absolutePath}"
                )
            }
        }

        if (langHasSkill) {
            languages.add(lang.name)
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
    return definitions
}
