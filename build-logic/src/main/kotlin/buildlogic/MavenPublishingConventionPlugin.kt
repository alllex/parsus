package buildlogic

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class MavenPublishingConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.dokka")
        project.pluginManager.apply("com.vanniktech.maven.publish")

        project.extensions.configure(MavenPublishBaseExtension::class.java) { ext ->
            ext.configure(KotlinMultiplatform(javadocJar = JavadocJar.Dokka("dokkaHtml")))
            ext.publishToMavenCentral()
            ext.signAllPublications()

            ext.pom { pom ->
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
