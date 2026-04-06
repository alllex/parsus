plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.allopen)
    implementation(libs.kotlinx.benchmark.plugin)
    implementation(libs.dokka.gradle.plugin)
}
