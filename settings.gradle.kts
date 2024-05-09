pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

include(":app")

// Uncomment these lines to use a local copy of the projects, instead of letting Gradle fetch them
// from Jitpack. You may want to change the paths in `includeBuild()` if you don't have those
// projects in the same folder as this project.

includeBuild("../dicio-skill") {
    dependencySubstitution {
        substitute(module("com.github.Stypox:dicio-skill")).using(project(":skill"))
    }
}

includeBuild("../dicio-numbers") {
    dependencySubstitution {
        substitute(module("com.github.Stypox:dicio-numbers")).using(project(":numbers"))
    }
}

includeBuild("../dicio-sentences-compiler") {
    dependencySubstitution {
        substitute(module("com.github.Stypox:dicio-sentences-compiler")).using(project(":sentences_compiler"))
    }
}
/**/
