package buildsrc.conventions

import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/** Base configuration for all Kotlin/Multiplatform projects */

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(8)

    kotlin {
        compilerOptions {
            languageVersion = KotlinVersion.KOTLIN_1_7
            apiVersion = KotlinVersion.KOTLIN_1_7
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
