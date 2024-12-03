package buildsrc.conventions

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_1_7
        apiVersion = KotlinVersion.KOTLIN_1_7
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
