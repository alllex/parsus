import buildsrc.utils.nativeTarget

plugins {
    buildsrc.conventions.`kotlin-multiplatform-base`
}

kotlin {
    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    nativeTarget()

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
