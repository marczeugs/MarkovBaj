import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")

    // Common
    kotlin("plugin.serialization")

    // JVM Backend (Bot + Janitor Backend + REST API)
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application

    // Website Frontend
    id("org.jetbrains.compose") version "1.5.11"
}

group = "marczeugs.markovbaj"
version = "3.5.1"

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

tasks.compileJava {
    targetCompatibility = "17"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
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
        attributes.attribute(Attribute.of("dummy", String::class.java), "KT-55751")
    }

    jvm("jvmScripts")


    val kotlinVersion: String by project
    val ktorVersion: String by project
    val exposedVersion: String by project
    val kotlinXSerializationVersion: String by project
    val kotlinXCoroutinesVersion: String by project
    val kordVersion: String by project

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("org.jetbrains.compose.web.ExperimentalComposeWebApi")
            }
        }

        val commonMain by getting {
            kotlin.srcDir(buildInfoGenerator.map { it.destinationDir })

            dependencies {
                implementation("io.github.microutils:kotlin-logging:2.1.23")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinXSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
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
                implementation(compose.html.core)
                implementation(compose.runtime)

                implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinXSerializationVersion")

                implementation(files("lib/JRAW-1.1.0.jar"))
                implementation("com.squareup.okhttp3:okhttp:4.10.0")
                implementation("com.squareup.moshi:moshi:1.13.0")

                implementation("org.slf4j:slf4j-api:2.0.0")
                implementation("org.slf4j:slf4j-simple:2.0.0")

                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
                implementation("io.ktor:ktor-server-host-common:$ktorVersion")
                implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
                implementation("io.ktor:ktor-server-sessions:$ktorVersion")
                implementation("io.ktor:ktor-server-resources:$ktorVersion")
                implementation("io.ktor:ktor-server-auth:$ktorVersion")
                implementation("io.ktor:ktor-server-cors:$ktorVersion")
                implementation("io.ktor:ktor-server-websockets:$ktorVersion")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")

                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
                implementation("org.postgresql:postgresql:42.6.0")

                implementation("org.jetbrains.kotlinx:kotlinx-html:0.8.0")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.463")

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
                implementation(compose.html.core)
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

// Builds in Gradle 8.0+ fail without this because the `jvm` and `jvmScripts` derived configurations have the same attributes
configurations {
    val dummyAttribute = Attribute.of("dummy", String::class.java)

    getByName("jvmScriptsApiElements") {
        attributes {
            attribute(dummyAttribute, "dummy")
        }
    }

    getByName("jvmScriptsRuntimeElements") {
        attributes {
            attribute(dummyAttribute, "dummy1")
        }
    }
}

application {
    mainClass.set("MarkovBajKt")
}