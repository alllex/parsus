plugins {
    buildsrc.conventions.`kotlin-multiplatform`
    buildsrc.conventions.`maven-publishing`
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jetbrains.dokka")
}

val publishVersion = project.layout.projectDirectory.file("version.txt").asFile.readText().trim()

group = "me.alllex.parsus"
version = publishVersion

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                compileOnly("org.jetbrains:annotations:26.0.1") {
                    because("multiplatform @Language annotation")
                }
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.willowtreeapps.assertk:assertk:0.26.1")
                runtimeOnly(kotlin("reflect"))
            }
        }
    }
}

repositories {
    mavenCentral()
}

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    description = "Produce javadoc with Dokka HTML inside"
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml)
    archiveClassifier = "javadoc"
}

// Without this there is a Gradle error (notice mismatch between publish task and sign names):
// > Reason: Task ':publishIosArm64PublicationToMavenLocal' uses this output of task ':signIosX64Publication' without declaring an explicit or implicit dependency.
tasks.withType<AbstractPublishToMaven>().configureEach {
    mustRunAfter(tasks.withType<Sign>())
}

// Maven Central publication requires a javadoc jar
publishing {
    publications.withType<MavenPublication>().configureEach {
        artifact(javadocJar)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl = uri("https://s01.oss.sonatype.org/service/local/")
            snapshotRepositoryUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
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
