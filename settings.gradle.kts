plugins {
    id("com.gradle.enterprise") version "3.14.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.6.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        if (settings.providers.environmentVariable("CI").orNull != null) {
            publishAlways()
            tag("CI")
        }
    }
}

rootProject.name = "parsus"

//include(":demo", ":benchmarks")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
