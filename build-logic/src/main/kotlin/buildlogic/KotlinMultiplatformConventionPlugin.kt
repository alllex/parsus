package buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType

/** Kotlin/Multiplatform convention that configures all targets for Parsus */
class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("parsus.kotlin-multiplatform-base")

        project.extensions.configure(KotlinMultiplatformExtension::class.java) { kotlin ->
            kotlin.jvm()

            kotlin.js(KotlinJsCompilerType.IR) { js ->
                js.browser()
                js.nodejs()
            }

            kotlin.linuxX64()
            kotlin.linuxArm64()

            kotlin.mingwX64()

            // TODO: fix build for Apple-related targets
//            kotlin.macosX64()
//            kotlin.macosArm64()

//            kotlin.iosX64()
//            kotlin.iosArm64()
        }
    }
}
