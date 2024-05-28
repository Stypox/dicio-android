package org.stypox.dicio.sentencesCompilerPlugin

import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.stypox.dicio.sentencesCompilerPlugin.data.SkillDefinitionsFile
import org.stypox.dicio.sentencesCompilerPlugin.data.parseYamlFile
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

    @TaskAction
    fun generateResource() {
        val inputDirFile = inputDir.get().asFile
        val outputDirFile = outputDir.get().asFile

        val definitions: SkillDefinitionsFile = parseYamlFile(File(inputDirFile, "skill_definitions.yml"))
        println(definitions)

        println(FileSpec.builder(PACKAGE_NAME, CLASS_NAME)
            .addFileComment(FILE_COMMENT + definitions.toString())
            .build()
            .writeTo(outputDirFile))
        println(outputDirFile)
    }
}
