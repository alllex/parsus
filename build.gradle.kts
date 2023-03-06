plugins {
    buildsrc.conventions.`kotlin-multiplatform`
    buildsrc.conventions.`maven-publishing`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "me.alllex.parsus"
version = "0.1.5-SNAPSHOT"

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.willowtreeapps.assertk:assertk:0.25")
            }
        }
    }
}

repositories {
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
