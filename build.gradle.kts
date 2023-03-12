plugins {
    buildsrc.conventions.`kotlin-multiplatform`
    buildsrc.conventions.`maven-publishing`
    id("io.github.gradle-nexus.publish-plugin") version "1.2.0"
    id("org.jetbrains.dokka")
}

group = "me.alllex.parsus"
version = "0.3.1-SNAPSHOT"

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.willowtreeapps.assertk:assertk:0.25")
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
    archiveClassifier.set("javadoc")
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
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
