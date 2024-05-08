// Top-level build file where you can add configuration options common to all sub-projects/modules.

allprojects {
    gradle.projectsEvaluated {
        tasks.withType<JavaCompile> {
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-Xlint:deprecation")
        }
    }
}
