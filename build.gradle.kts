plugins {
    val kotlinVersion: String by System.getProperties()

    kotlin("multiplatform") version kotlinVersion

    // Common
    kotlin("plugin.serialization") version kotlinVersion

    // JVM Backend (Bot + Janitor Backend + REST API)
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application

    // Website Frontend
    id("org.jetbrains.compose") version "1.3.0-rc01"
}

group = "marczeugs.markovbaj"
version = "3.0.1"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

    // Kord snapshots
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val buildInfoGenerator by tasks.registering(Sync::class) {
    from(
        resources.text.fromString(
            """
                object BuildInfo {
                    const val PROJECT_VERSION = "${project.version}"
                    const val PROJECT_BUILD_TIMESTAMP_MILLIS = ${System.currentTimeMillis()}
                }
            """.trimIndent()
        )
    ) {
        rename { "BuildInfo.kt" }
    }

    into(layout.buildDirectory.dir("generated/kotlin/"))
}

tasks.build {
    dependsOn(buildInfoGenerator)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                devServer?.`open` = false
            }
        }

        binaries.executable()
    }

    jvm {
        withJava()
    }

    jvm("jvmScripts")


    val kotlinVersion: String by System.getProperties()
    val ktorVersion: String by System.getProperties()
    val kotlinXSerializationVersion: String by System.getProperties()
    val kotlinXCoroutinesVersion: String by System.getProperties()
    val kordVersion: String by System.getProperties()

    sourceSets {
        all {
            languageSettings.apply {
                languageVersion = "1.8"

                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("org.jetbrains.compose.web.ExperimentalComposeWebApi")
            }
        }

        val commonMain by getting {
            kotlin.srcDir(buildInfoGenerator.map { it.destinationDir })

            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinXSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinXSerializationVersion")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)

                implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinXSerializationVersion")

                implementation(files("lib/JRAW-1.1.0.jar"))
                implementation("com.squareup.okhttp3:okhttp:4.10.0")
                implementation("com.squareup.moshi:moshi:1.13.0")

                implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
                implementation("org.slf4j:slf4j-api:2.0.0")
                implementation("org.slf4j:slf4j-simple:2.0.0")

                implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-sessions-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-resources-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-apache-jvm:$ktorVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.463")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")

                implementation("dev.kord:kord-core:$kordVersion") {
                    capabilities {
                        requireCapability("dev.kord:core-voice:$kordVersion")
                    }
                }

                implementation("com.github.twitch4j:twitch4j:1.12.0")
            }
        }

        val jvmScriptsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)

                implementation(kotlin("script-runtime"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinXSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
                implementation("org.slf4j:slf4j-api:2.0.0")
                implementation("org.slf4j:slf4j-simple:2.0.0")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
    }
}

application {
    mainClass.set("MarkovBajKt")
}