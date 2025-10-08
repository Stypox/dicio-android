package org.stypox.dicio.unicodeCldrPlugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

class UnicodeCldrPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val languagesTask = target.tasks.create("unicodeCldrLanguages", UnicodeCldrLanguagesTask::class.java)

        // make sure the generated kotlin files are compiled by adding them to the source sets; note
        // that this also makes sure any task depending on source sets also depends on this task
        // https://slack-chats.kotlinlang.org/t/486810
        target.extensions
            .getByType(KotlinProjectExtension::class.java)
            .sourceSets
            .getByName("main")
            .kotlin
            .srcDir(languagesTask.outputDir)
        target.tasks
            .matching { it.name.contains("Kotlin") }
            .configureEach { inputs.dir(languagesTask.outputDir) }
    }
}
