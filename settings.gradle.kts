import me.champeau.gradle.igp.gitRepositories
import org.eclipse.jgit.api.Git
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Properties

include(":app")
include(":skill")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // not using version catalog because it is not available in settings.gradle.kts
    id("me.champeau.includegit") version "0.1.6"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}


// All of the code below handles depending on libraries from git repos, in particular dicio-numbers,
// dicio-skill and dicio-sentences-compiler. The git commits to checkout can be updated here.
// If you want to use a local copy of the projects (provided that you have cloned them in
// `../dicio-*`), you can add `useLocalDicioLibraries=true` in `local.properties`.

data class IncludeGitRepo(
    val name: String,
    val uri: String,
    val projectPath: String,
    val commit: String,
)

val includeGitRepos = listOf(
    IncludeGitRepo(
        name = "dicio-numbers",
        uri = "https://github.com/Stypox/dicio-numbers",
        projectPath = ":numbers",
        commit = "3206f161e80b349168fedcc21a601e5ea9b05961",
    ),
    IncludeGitRepo(
        name = "dicio-sentences-compiler",
        uri = "https://github.com/Stypox/dicio-sentences-compiler",
        projectPath = ":sentences_compiler",
        commit = "6862bd9b351ca55a457fd3f88ad764d5b7e71543",
    ),
)

val localProperties = Properties().apply {
    try {
        load(FileInputStream(File(rootDir, "local.properties")))
    } catch (e: Throwable) {
        println("Warning: can't read local.properties: $e")
    }
}

if (localProperties.getOrDefault("useLocalDicioLibraries", "") == "true") {
    for (repo in includeGitRepos) {
        includeBuild("../${repo.name}") {
            dependencySubstitution {
                substitute(module("git.included.build:${repo.name}"))
                    .using(project(repo.projectPath))
            }
        }
    }

} else {
    // if the repo has already been cloned, the gitRepositories plugin is buggy and doesn't
    // fetch the remote repo before trying to checkout the commit (in case the commit has changed),
    // so we need to do it manually
    for (repo in includeGitRepos) {
        val file = File("$rootDir/checkouts/${repo.name}")
        if (file.isDirectory) {
            Git.open(file).fetch().call()
        }
    }

    gitRepositories {
        for (repo in includeGitRepos) {
            include(repo.name) {
                uri.set(repo.uri)
                commit.set(repo.commit)
                autoInclude.set(false)
                includeBuild("") {
                    dependencySubstitution {
                        substitute(module("git.included.build:${repo.name}"))
                            .using(project(repo.projectPath))
                    }
                }
            }
        }
    }
}
