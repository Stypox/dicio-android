/*
 * Taken from /e/OS Assistant
 *
 * Copyright (C) 2024 MURENA SAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


group = "org.stypox.dicio.sentencesCompilerPlugin"

plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("sentences-compiler-plugin") {
            id = "org.stypox.dicio.sentencesCompilerPlugin"
            implementationClass = "org.stypox.dicio.sentencesCompilerPlugin.SentencesCompilerPlugin"
        }
    }
}

dependencies {
    // these dependencies are usually compile-time dependencies, but since this is a plugin, we want
    // to access the gradle libraries at the runtime of the plugin, which happens at compile-time
    // for the app
    implementation(libs.android.tools.build.gradle)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlinpoet)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.dicio.sentences.compiler)
}
