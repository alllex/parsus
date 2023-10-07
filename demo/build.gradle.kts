import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    kotlin("multiplatform") version "1.9.10"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    jvmToolchain(8)
//
//    js(IR) {
//        browser()
//        nodejs()
//    }
//
//    nativeTarget()

    sourceSets {
        commonMain {
            dependencies {
//                implementation("me.alllex.parsus:parsus")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
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
