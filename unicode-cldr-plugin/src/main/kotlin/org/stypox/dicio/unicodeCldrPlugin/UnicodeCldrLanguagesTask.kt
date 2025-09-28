package org.stypox.dicio.unicodeCldrPlugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.stypox.dicio.unicodeCldrPlugin.data.ensureGitRepoDownloaded
import org.stypox.dicio.unicodeCldrPlugin.data.parseLanguages
import org.stypox.dicio.unicodeCldrPlugin.gen.generateSkillSentencesKt
import org.stypox.dicio.unicodeCldrPlugin.util.CLDR_CHECKOUT_PATH
import org.stypox.dicio.unicodeCldrPlugin.util.CLDR_LANGUAGES_PATH
import org.stypox.dicio.unicodeCldrPlugin.util.CLDR_REPO
import org.stypox.dicio.unicodeCldrPlugin.util.UnicodeCldrPluginException
import java.io.File

abstract class UnicodeCldrLanguagesTask : DefaultTask() {

    /**
     * Which git commit of the https://github.com/unicode-org/cldr repo to use as a source of data.
     */
    @get:Input
    abstract val unicodeCldrGitCommit: Property<String>

    @InputFile
    val dicioLanguagesFile: RegularFileProperty = project.objects.fileProperty().apply {
        set(project.file("src/main/proto/language.proto"))
    }

    @OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty().apply {
        set(project.layout.buildDirectory.dir("generated/unicode_cldr_plugin"))
    }

    @Throws(UnicodeCldrPluginException::class)
    @TaskAction
    fun generateResource() {
        // the same place where the includegit plugin clones repositories
        val checkoutsFolder = File(project.rootProject.rootDir, CLDR_CHECKOUT_PATH)

        ensureGitRepoDownloaded(
            repo = CLDR_REPO,
            commit = unicodeCldrGitCommit.get(),
            directory = checkoutsFolder
        )

        val data = parseLanguages(
            dicioLanguagesFile = dicioLanguagesFile.get().asFile,
            cldrLanguagesDir = File(checkoutsFolder, CLDR_LANGUAGES_PATH),
        )

        generateSkillSentencesKt(data, outputDir.asFile.get())
    }
}
