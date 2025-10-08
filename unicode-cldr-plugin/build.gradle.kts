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


group = "org.stypox.dicio.unicodeCldrPlugin"

plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("unicode-cldr-plugin") {
            id = "org.stypox.dicio.unicodeCldrPlugin"
            implementationClass = "org.stypox.dicio.unicodeCldrPlugin.UnicodeCldrPlugin"
        }
    }
}


java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}


dependencies {
    // these dependencies are usually compile-time dependencies, but since this is a plugin, we want
    // to access the gradle libraries at the runtime of the plugin, which happens at compile-time
    // for the app
    implementation(libs.android.tools.build.gradle)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlinpoet)
    implementation(libs.jgit)

    // also depending on sentences compiler for nfkdNormalize
    implementation(libs.dicio.sentences.compiler)
}
