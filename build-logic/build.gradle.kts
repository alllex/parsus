plugins {
    // Keep in sync with libs.versions.toml kotlin-plugin version
    kotlin("jvm") version "2.3.20"
    `java-gradle-plugin`
}

kotlin {
    compilerOptions {
        // Target Gradle's embedded Kotlin API version for compatibility
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.allopen)
    implementation(libs.kotlinx.benchmark.plugin)
    implementation(libs.dokka.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kotlin-jvm") {
            id = "parsus.kotlin-jvm"
            implementationClass = "buildlogic.KotlinJvmConventionPlugin"
        }
        register("kotlin-multiplatform-base") {
            id = "parsus.kotlin-multiplatform-base"
            implementationClass = "buildlogic.KotlinMultiplatformBaseConventionPlugin"
        }
        register("kotlin-multiplatform") {
            id = "parsus.kotlin-multiplatform"
            implementationClass = "buildlogic.KotlinMultiplatformConventionPlugin"
        }
        register("maven-publishing") {
            id = "parsus.maven-publishing"
            implementationClass = "buildlogic.MavenPublishingConventionPlugin"
        }
    }
}
