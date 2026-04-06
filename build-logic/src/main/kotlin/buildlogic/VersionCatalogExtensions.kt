package buildlogic

import org.gradle.api.artifacts.VersionCatalog
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

val VersionCatalog.jvmToolchainVersion: Int
    get() = findVersion("jvm-toolchain").get().requiredVersion.toInt()

val VersionCatalog.targetKotlinVersion: KotlinVersion
    get() = KotlinVersion.fromVersion(findVersion("kotlin-target").get().requiredVersion.substringBeforeLast("."))
