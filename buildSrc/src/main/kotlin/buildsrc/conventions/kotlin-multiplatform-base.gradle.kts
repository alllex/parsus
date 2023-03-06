package buildsrc.conventions

import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/** Base configuration for all Kotlin/Multiplatform projects */

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(8)

    targets.configureEach {
        compilations.configureEach {
            kotlinOptions {
                apiVersion = "1.7"
                languageVersion = "1.7"
            }
        }
    }

    // configure all Kotlin/JVM Tests to use JUnitPlatform
    targets.withType<KotlinJvmTarget>().configureEach {
        testRuns.configureEach {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
}
