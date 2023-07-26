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
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project

        if (signingKeyId != null) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications)
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "Parsus"
            description = "Composable parsers using Kotlin Coroutines"
            url = "https://github.com/alllex/parsus"
            licenses {
                license {
                    name = "MIT"
                    url = "https://opensource.org/licenses/MIT"
                }
            }
            developers {
                developer {
                    id = "alllex"
                    name = "Alex by Software"
                    email = "software@alllex.me"
                    url = "https://alllex.me"
                }
            }
            scm {
                connection = "scm:git:git@github.com:alllex/parsus.git"
                developerConnection = "scm:git:git@github.com:alllex/parsus.git"
                url = "https://github.com/alllex/parsus"
            }
        }
    }
}
