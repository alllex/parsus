plugins {
    buildsrc.conventions.`kotlin-jvm`
}

dependencies {
    implementation(projects.parsus)
    testImplementation(kotlin("test"))
}

repositories {
    mavenCentral()
}
