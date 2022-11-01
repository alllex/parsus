import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

dependencies {
    implementation(rootProject)
    testImplementation(kotlin("test-junit"))
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "${JavaVersion.VERSION_11}"
}
