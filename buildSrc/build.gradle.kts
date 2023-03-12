plugins {
    `kotlin-dsl`
}

dependencies {
    val kotlinVer = "1.8.10"
    implementation(platform(kotlin("bom", kotlinVer)))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVer")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVer")
}
