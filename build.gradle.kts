plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "org.dicio.skill"
    compileSdk = 34
    defaultConfig {
        minSdk = 14 // must be at least 4, otherwise some unwanted permissions are requested
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.Stypox"
                artifactId = "dicio-skill"
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    // Android
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    // Dummy dicio-numbers implementation to bring ParserFormatter into the classpath
    implementation(libs.dicio.numbers)

    // Compose (check out https://developer.android.com/jetpack/compose/bom/bom-mapping)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.test.android.compose.ui.test.junit4)
    debugImplementation(libs.debug.compose.ui.tooling)
    debugImplementation(libs.debug.compose.ui.test.manifest)

    // Testing
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
}
