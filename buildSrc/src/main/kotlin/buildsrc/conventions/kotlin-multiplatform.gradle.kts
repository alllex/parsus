package buildsrc.conventions

import buildsrc.utils.nativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("multiplatform")
}

kotlin {
  jvm()
  jvmToolchain(8)

  js(IR) {
    browser()
    nodejs()
  }

  nativeTarget()

  sourceSets {
    val commonMain by getting
    val commonTest by getting

    val nativeMain by getting { dependsOn(commonMain) }
    val nativeTest by getting { dependsOn(commonTest) }
  }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.languageVersion = "1.7"
    kotlinOptions.apiVersion = "1.7"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
