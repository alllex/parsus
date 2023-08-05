package buildsrc.utils

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * Create a Kotlin/Native target based on the current OS.
 *
 * The target is named [name], and its source sets, `nativeMain` and `nativeTest`, should be
 * configured to extend the `commonMain` and `commonTest` source sets.
 */
fun KotlinMultiplatformExtension.nativeTarget(
    name: String = "native",
    currentHost: KonanTarget = HostManager.host,
    configure: KotlinNativeTargetWithHostTests.() -> Unit = { },
): KotlinNativeTargetWithHostTests {
    return when (currentHost) {
        KonanTarget.LINUX_X64 -> linuxX64(name, configure)
        KonanTarget.MACOS_X64 -> macosX64(name, configure)
        KonanTarget.MACOS_ARM64 -> macosArm64(name, configure)
        KonanTarget.MINGW_X64 -> mingwX64(name, configure)
        else -> error("unsupported host '${currentHost.name}'")
    }
}
