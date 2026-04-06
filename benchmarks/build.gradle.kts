plugins {
    buildsrc.conventions.`kotlin-jvm`
    kotlin("plugin.allopen")
    id("org.jetbrains.kotlinx.benchmark")
}

dependencies {
    implementation(projects.parsus)
    implementation(libs.kotlinx.benchmark.runtime)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}

repositories {
    mavenCentral()
}

allOpen.annotation("org.openjdk.jmh.annotations.State")

benchmark {
    targets.register("main")
    configurations["main"].apply {
        warmups = 3
        iterations = 5
        outputTimeUnit = "s"
    }
}
