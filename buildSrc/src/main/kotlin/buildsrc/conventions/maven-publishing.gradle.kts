package buildsrc.conventions

plugins {
    `maven-publish`
    signing
}

// Gradle hasn't updated the signing plugin to be compatible with lazy-configuration, so it needs weird workarounds:
afterEvaluate {
    // Register signatures in afterEvaluate, otherwise the signing plugin creates the signing tasks
    // too early, before all the publications are added.
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project

        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
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
