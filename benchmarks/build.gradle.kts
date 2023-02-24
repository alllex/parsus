plugins {
    java
    kotlin("jvm")
    kotlin("plugin.allopen") version "1.8.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.6"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

sourceSets.all {
    java.setSrcDirs(listOf("$name/src"))
    resources.setSrcDirs(listOf("$name/resources"))
}

dependencies {
    implementation(rootProject)
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime-jvm:0.4.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    testImplementation(kotlin("test-junit"))
}

repositories {
    mavenCentral()
}

allOpen.annotation("org.openjdk.jmh.annotations.State")

benchmark {
    targets.register("main")
    configurations["main"].apply {
        warmups = 5
        iterations = 10
        outputTimeUnit = "s"
    }
}
