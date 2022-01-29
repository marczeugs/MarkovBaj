plugins {
    val kotlinVersion = "1.6.10"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

group = "marczeugs"
version = "2.3"

buildscript {
    dependencies {
        classpath(kotlin("serialization", "1.6.10"))
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xopt-in=kotlin.time.ExperimentalTime"
        )

        jvmTarget = "1.8"
    }
}

tasks.processResources {
    filesMatching("buildinfo.properties") {
        expand(project.properties)
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC3")

    implementation(files("lib/JRAW-1.1.0.jar"))
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.moshi:moshi:1.13.0")

    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
}

tasks.withType<Jar> {
    manifest {
        attributes["Class-Path"] = configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        attributes["Main-Class"] = "MarkovBaj"
    }
}