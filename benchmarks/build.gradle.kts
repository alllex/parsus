plugins {
    buildsrc.conventions.`kotlin-jvm`
    kotlin("plugin.allopen")
    id("org.jetbrains.kotlinx.benchmark")
}

dependencies {
    implementation(projects.parsus)
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
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
