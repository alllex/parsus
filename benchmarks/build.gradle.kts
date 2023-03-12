plugins {
    buildsrc.conventions.`kotlin-jvm`
    kotlin("plugin.allopen") version "1.8.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.6"
}

dependencies {
    implementation(projects.parsus)
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
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
