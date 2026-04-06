package buildsrc.conventions

import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/** Base configuration for all Kotlin/Multiplatform projects */

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(17)

    kotlin {
        compilerOptions {
            languageVersion = KotlinVersion.KOTLIN_2_1
            apiVersion = KotlinVersion.KOTLIN_2_1
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
