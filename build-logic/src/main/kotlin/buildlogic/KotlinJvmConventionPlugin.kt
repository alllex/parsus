package buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")

        val libs = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        project.extensions.configure(KotlinJvmExtension::class.java) { kotlin ->
            kotlin.compilerOptions {
                languageVersion.set(libs.targetKotlinVersion)
                apiVersion.set(libs.targetKotlinVersion)
            }
            kotlin.jvmToolchain(libs.jvmToolchainVersion)
        }

        project.tasks.named("test", Test::class.java) { test ->
            test.useJUnitPlatform()
        }
    }
}
