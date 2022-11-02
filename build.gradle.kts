import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    signing
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

group = "me.alllex.parsus"
version = "0.1.5-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test-junit"))
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.23.1")
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "${JavaVersion.VERSION_11}"
}

kover {
    htmlReport {
        reportDir.set(layout.buildDirectory.dir("reports/kover/my-html"))
    }

    verify {
        onCheck.set(true)
        rule {
            bound {
                minValue = 75
                counter = kotlinx.kover.api.CounterType.LINE
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}

koverMerged {
    enable()
    filters {
        classes {
            excludes += "me.alllex.parsus.token.*"
        }
        projects {
            excludes += "benchmarks"
        }
    }

    htmlReport {
        reportDir.set(layout.buildDirectory.dir("reports/kover/my-merged-html"))
    }

    verify {
        onCheck.set(true)

        rule {
            isEnabled = true
            overrideClassFilter {
                includes += "me.alllex.parsus.parser.*"
            }

            bound {
                minValue = 90
                counter = kotlinx.kover.api.CounterType.LINE
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

configure<SigningExtension> {
    val signingKey: String? by project
    val signingPassword: String? by project
    val publishing: PublishingExtension by project

    if (signingKey == null || signingPassword == null) return@configure
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
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
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

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
}
