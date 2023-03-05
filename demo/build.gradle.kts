plugins {
    kotlin("jvm")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(rootProject)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}
