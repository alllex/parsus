package buildsrc.conventions

/** Kotlin/Multiplatform convention that configures all targets for Parsus */

plugins {
    id("buildsrc.conventions.kotlin-multiplatform-base")
}

kotlin {
    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    linuxX64()

    mingwX64()

    macosX64()
    macosArm64()

    iosX64()
    iosArm64()
}
