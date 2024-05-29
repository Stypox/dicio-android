package org.stypox.dicio.sentencesCompilerPlugin

import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.stypox.dicio.sentencesCompilerPlugin.data.extractDataFromFiles
import org.stypox.dicio.sentencesCompilerPlugin.data.parseSentences
import org.stypox.dicio.sentencesCompilerPlugin.util.CLASS_NAME
import org.stypox.dicio.sentencesCompilerPlugin.util.FILE_COMMENT
import org.stypox.dicio.sentencesCompilerPlugin.util.PACKAGE_NAME
import org.stypox.dicio.sentencesCompilerPlugin.util.SentencesCompilerPluginException

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

        val rawData = extractDataFromFiles(logger, inputDirFile)
        val parsedData = rawData.map(::parseSentences)
        parsedData.forEach(::checkSentences)

        FileSpec.builder(PACKAGE_NAME, CLASS_NAME)
            .addFileComment(FILE_COMMENT + parsedData.toString())
            .build()
            .writeTo(outputDirFile)
    }
}
