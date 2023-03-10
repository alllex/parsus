import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    signing
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.2.0"
}

group = "me.alllex.parsus"
version = "0.2.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
}

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.languageVersion = "1.7"
    kotlinOptions.apiVersion = "1.7"
}

tasks.test {
    useJUnitPlatform()
}

signing {
    sign(publishing.publications)

    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
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
                        name.set("Alex by Software")
                        email.set("software@alllex.me")
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
