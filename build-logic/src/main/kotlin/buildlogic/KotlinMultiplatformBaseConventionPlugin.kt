package buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/** Base configuration for all Kotlin/Multiplatform projects */
class KotlinMultiplatformBaseConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")

        val libs = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        project.extensions.configure(KotlinMultiplatformExtension::class.java) { kotlin ->
            kotlin.jvmToolchain(libs.jvmToolchainVersion)

            kotlin.compilerOptions {
                languageVersion.set(libs.targetKotlinVersion)
                apiVersion.set(libs.targetKotlinVersion)
            }

            // configure all Kotlin/JVM Tests to use JUnitPlatform
            kotlin.targets.withType(KotlinJvmTarget::class.java).configureEach { target ->
                target.testRuns.configureEach { testRun ->
                    testRun.executionTask.configure { task ->
                        task.useJUnitPlatform()
                    }
                }
            }
        }
    }
}
