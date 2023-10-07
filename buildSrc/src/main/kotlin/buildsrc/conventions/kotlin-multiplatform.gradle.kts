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
//    macosArm64()
//    ios() // shortcut for iosArm64, iosX64

    // Native targets all extend commonMain and commonTest.
    //
    // Some targets (ios, tvos, watchos) are shortcuts provided by the Kotlin DSL, that
    // provide additional targets, except for 'simulators' which must be defined manually.
    // https://kotlinlang.org/docs/multiplatform-share-on-platforms.html#use-target-shortcuts
    //
    // common
    // └── native
    //     ├── linuxX64
    //     ├── mingwX64
    //     ├── macosX64
    //     ├── macosArm64
    //     └── ios (shortcut)
    //         ├── iosArm64
    //         └── iosX64

//    @Suppress("UNUSED_VARIABLE")
//    sourceSets {
//        val commonMain by getting
//        val commonTest by getting
//
//        val nativeMain by creating { dependsOn(commonMain) }
//        val nativeTest by creating { dependsOn(commonTest) }
//
//        // Linux
//        val linuxX64Main by getting { dependsOn(nativeMain) }
//        val linuxX64Test by getting { dependsOn(nativeTest) }
//
//        // Windows - MinGW
//        val mingwX64Main by getting { dependsOn(nativeMain) }
//        val mingwX64Test by getting { dependsOn(nativeTest) }
//
//        // Apple - macOS
//        val macosArm64Main by getting { dependsOn(nativeMain) }
//        val macosArm64Test by getting { dependsOn(nativeTest) }
//
//        val macosX64Main by getting { dependsOn(nativeMain) }
//        val macosX64Test by getting { dependsOn(nativeTest) }
//
//        // Apple - iOS
//        val iosMain by getting { dependsOn(nativeMain) }
//        val iosTest by getting { dependsOn(nativeTest) }
//    }
}
