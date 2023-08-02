package buildsrc.conventions

import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/** Base configuration for all Kotlin/Multiplatform projects */

plugins {
    kotlin("multiplatform")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.6.0"
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
