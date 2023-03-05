import buildsrc.utils.nativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    signing
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "me.alllex.parsus"
version = "0.1.5-SNAPSHOT"

kotlin {

  jvm()
  jvmToolchain(8)

  js(IR) {
    browser()
    nodejs()
  }

  nativeTarget()

  sourceSets {
    val commonMain by getting {}
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("com.willowtreeapps.assertk:assertk:0.25")
      }
    }

    val nativeMain by getting { dependsOn(commonMain) }
    val nativeTest by getting { dependsOn(commonTest) }
  }
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.languageVersion = "1.7"
    kotlinOptions.apiVersion = "1.7"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    val publishing: PublishingExtension by project

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Parsus")
            description.set("Composable parsers using Kotlin Coroutines")
            url.set("https://github.com/alllex/parsus")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("alllex")
                    name.set("Aleksei Semin")
                    email.set("alllexsm@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git@github.com:alllex/parsus.git")
                developerConnection.set("scm:git:git@github.com:alllex/parsus.git")
                url.set("https://github.com/alllex/parsus")
            }
        }
    }
}
