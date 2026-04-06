plugins {
    buildsrc.conventions.`kotlin-multiplatform-base`
}

kotlin {
    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    // TODO: fix demo for native targets
    // nativeTarget()

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.parsus)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

repositories {
    mavenCentral()
}
