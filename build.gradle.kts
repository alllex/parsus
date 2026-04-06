import buildlogic.GenerateQuickReferenceMarkdown

plugins {
    id("parsus.kotlin-multiplatform")
    id("parsus.maven-publishing")
}

val publishVersion = project.layout.projectDirectory.file("version.txt").asFile.readText().trim()

group = "me.alllex.parsus"
version = publishVersion

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.assertk)
                runtimeOnly(kotlin("reflect"))
            }
        }
    }
}

repositories {
    mavenCentral()
}


tasks.register<GenerateQuickReferenceMarkdown>("generateQuickRef") {
    kotlinTestSource = project.layout.projectDirectory.file("src/commonTest/kotlin/me/alllex/parsus/ReadmeTests.kt")
    markdownOutput = project.layout.buildDirectory.file("quickref.md")
}

tasks.register("checkQuickRefInReadme") {
    dependsOn("generateQuickRef")
    val generatedQuickRef = project.layout.buildDirectory.file("quickref.md")
    val readme = project.layout.projectDirectory.file("README.md")
    inputs.file(generatedQuickRef)
    inputs.file(readme)
    doLast {
        val generatedQuickRefString = generatedQuickRef.get().asFile.readText()
        val readmeAsString = readme.asFile.readText()
        check(readmeAsString.contains(generatedQuickRefString)) {
            "Generated quick reference is not present in README.md"
        }
    }
}

tasks.named("check") {
    dependsOn("checkQuickRefInReadme")
}
