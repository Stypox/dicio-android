package org.stypox.dicio.sentencesCompilerPlugin

import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.stypox.dicio.sentencesCompilerPlugin.data.SkillDefinitionsFile
import org.stypox.dicio.sentencesCompilerPlugin.data.parseYamlFile
import org.stypox.dicio.sentencesCompilerPlugin.util.CLASS_NAME
import org.stypox.dicio.sentencesCompilerPlugin.util.FILE_COMMENT
import org.stypox.dicio.sentencesCompilerPlugin.util.PACKAGE_NAME
import org.stypox.dicio.sentencesCompilerPlugin.util.SKILL_DEFINITIONS_FILE
import org.stypox.dicio.sentencesCompilerPlugin.util.SentencesCompilerPluginException
import org.stypox.dicio.sentencesCompilerPlugin.util.YML_EXT
import java.io.File

open class SentencesCompilerTask : DefaultTask() {
    @InputDirectory
    val inputDir: DirectoryProperty = project.objects.directoryProperty().apply {
        set(project.file("src/main/sentences"))
    }

    @OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty().apply {
        set(project.layout.buildDirectory.dir("generated/sentences_compiler_plugin"))
    }

    @Throws(SentencesCompilerPluginException::class)
    @TaskAction
    fun generateResource() {
        val inputDirFile = inputDir.get().asFile
        val outputDirFile = outputDir.get().asFile

        val definitions: SkillDefinitionsFile = parseYamlFile(File(inputDirFile, SKILL_DEFINITIONS_FILE))
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
                    throw SentencesCompilerPluginException("Skill sentences file ${lang.name}/${
                        file.name} is missing these sentence ids ${
                        expectedSentenceIds - sentences.keys}: ${file.absolutePath}")
                } else if (!expectedSentenceIds.containsAll(sentences.keys)) {
                    throw SentencesCompilerPluginException("Skill sentences file ${lang.name}/${
                        file.name} has these superfluous sentence ids ${
                        sentences.keys - expectedSentenceIds}: ${file.absolutePath}")
                }

                val emptySentences = sentences.filter { it.value.isNullOrEmpty() }.map { it.key }
                if (emptySentences.isNotEmpty()) {
                    throw SentencesCompilerPluginException("Skill sentences file ${lang.name}/${
                        file.name} has no sentence definitions for these sentence ids ${
                        emptySentences}: ${file.absolutePath}")
                }
            }

            if (langHasSkill) {
                languages.add(lang.name)
            }

            // issue a warning for unknown files
            for (file in lang.listFiles()!!) {
                if (definitions.skills.all { it.id + YML_EXT != file.name }) {
                    logger.error("[Warning] Skill sentences file ${lang.name}/${file.name
                        } does not correspond to any skill: ${file.absolutePath}")
                }
            }
        }

        FileSpec.builder(PACKAGE_NAME, CLASS_NAME)
            .addFileComment(FILE_COMMENT + definitions.toString())
            .build()
            .writeTo(outputDirFile)
    }
}
