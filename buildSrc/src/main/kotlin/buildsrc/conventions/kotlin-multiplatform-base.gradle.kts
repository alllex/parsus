package buildsrc.conventions

import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/** Base configuration for all Kotlin/Multiplatform projects */

plugins {
    kotlin("multiplatform")
}

private val libs = versionCatalogs.named("libs")

kotlin {
    jvmToolchain(libs.jvmToolchainVersion)

    kotlin {
        compilerOptions {
            languageVersion = libs.targetKotlinVersion
            apiVersion = libs.targetKotlinVersion
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
