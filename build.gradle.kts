plugins {
    val kotlinVersion = "1.7.0"

    application

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.graalvm.buildtools.native") version "0.9.4"
}

group = "marczeugs"
version = "2.4.4"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

application {
    mainClass.set("MarkovBajKt")
}

tasks.compileKotlin {
    kotlin.sourceSets.all {
        languageSettings.apply {
            optIn("kotlinx.serialization.ExperimentalSerializationApi")
            optIn("kotlin.time.ExperimentalTime")
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.processResources {
    filesMatching("buildinfo.properties") {
        expand(project.properties)
    }
}

dependencies {
    val ktorVersion = "2.0.2"

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")

    implementation(files("lib/JRAW-1.1.0.jar"))
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.moshi:moshi:1.13.0")

    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    implementation(kotlin("script-runtime"))
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
}

nativeBuild {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.matching("GraalVM"))
    })

    mainClass.set("MarkovBajKt")
    //configurationFileDirectories.from(file("native-image-config"))

    buildArgs.apply {
        add("--libc=musl")
        add("--static")
    }
}