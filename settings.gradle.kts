plugins {
    id("com.gradle.develocity") version "3.18.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        val ci = providers.environmentVariable("CI").orNull != null
        publishing { onlyIf { ci } }
        obfuscation { externalProcessName { "non-build-process" } }
    }
}

rootProject.name = "parsus"

include(":demo", ":benchmarks")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
