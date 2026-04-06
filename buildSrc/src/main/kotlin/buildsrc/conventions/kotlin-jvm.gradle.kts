package buildsrc.conventions

plugins {
    kotlin("jvm")
}

private val libs = versionCatalogs.named("libs")

kotlin {
    compilerOptions {
        languageVersion = libs.targetKotlinVersion
        apiVersion = libs.targetKotlinVersion
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(libs.jvmToolchainVersion)
}
