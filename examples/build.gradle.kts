plugins {
    kotlin("jvm") version "1.9.23"
    application
}

subprojects {
    repositories {
        mavenCentral()
        google()
    }
}


version = "0.1.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

application {
    mainClass.set("MainKt")
}
