package buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

class MavenPublishingConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("maven-publish")
        project.pluginManager.apply("signing")

        // Gradle hasn't updated the signing plugin to be compatible with lazy-configuration, so it needs weird workarounds:
        project.afterEvaluate {
            // Register signatures in afterEvaluate, otherwise the signing plugin creates the signing tasks
            // too early, before all the publications are added.
            project.extensions.configure(SigningExtension::class.java) { signing ->
                val signingKeyId: String? = project.findProperty("signingKeyId") as? String
                val signingKey: String? = project.findProperty("signingKey") as? String
                val signingPassword: String? = project.findProperty("signingPassword") as? String

                if (signingKeyId != null) {
                    signing.useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
                    signing.sign(project.extensions.getByType(PublishingExtension::class.java).publications)
                }
            }
        }

        project.extensions.configure(PublishingExtension::class.java) { publishing ->
            publishing.publications.withType(MavenPublication::class.java).configureEach { publication ->
                publication.pom { pom ->
                    pom.name.set("Parsus")
                    pom.description.set("Composable parsers using Kotlin Coroutines")
                    pom.url.set("https://github.com/alllex/parsus")
                    pom.licenses { licenses ->
                        licenses.license { license ->
                            license.name.set("MIT")
                            license.url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    pom.developers { developers ->
                        developers.developer { developer ->
                            developer.id.set("alllex")
                            developer.name.set("Alex by Software")
                            developer.email.set("software@alllex.me")
                            developer.url.set("https://alllex.me")
                        }
                    }
                    pom.scm { scm ->
                        scm.connection.set("scm:git:git@github.com:alllex/parsus.git")
                        scm.developerConnection.set("scm:git:git@github.com:alllex/parsus.git")
                        scm.url.set("https://github.com/alllex/parsus")
                    }
                }
            }
        }
    }
}
