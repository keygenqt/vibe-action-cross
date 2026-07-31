import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask
import java.io.PipedOutputStream
import java.io.PipedInputStream
import kotlin.concurrent.thread

plugins {
    // Resolved without version — comes from root classpath
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
    // buildSrc precompiled script plugin, not a Plugin Portal id
    id("spotless")
    // Version from libs.versions.toml
    alias(libs.plugins.composeCompiler)
}

tasks {
    buildPlugin {
        archiveBaseName.set("vibe-action-plugin")
    }
}

dependencies {
    testImplementation(libs.junit)

    implementation(libs.compose.material3) { excludePlatformProvided() }
    implementation(libs.koin.compose) { excludePlatformProvided() }
    implementation(libs.navigation3.ui) { excludePlatformProvided() }
    implementation(libs.navigationevent.compose) { excludePlatformProvided() }
    implementation(libs.compose.material.icons.core) { excludePlatformProvided() }
    implementation(libs.compose.material.icons.extended) { excludePlatformProvided() }

    intellijPlatform {
        intellijIdea("2026.1.4")

        @Suppress("UnstableApiUsage")
        composeUI()

        pluginComposedModule(implementation(projects.pluginUi.shared))

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
    }
}

// kotlinx-coroutines-core and kotlinx-serialization-core ship together with
// bundledPlugin("org.jetbrains.kotlin"). IMPORTANT: runtimeClasspath only, not
// configurations.all — the latter also breaks the Kotlin compiler's own classpath
// (it needs its own copies of both for the K2 daemon).
configurations.named("runtimeClasspath") {
    // Coroutines – already provided by bundled Kotlin plugin
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
    // Serialization – provided by IDE
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core")
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core-jvm")
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json")
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json-jvm")
}

/**
 * Filters runIde output to show only lines containing the project's group tag.
 */
tasks.withType<RunIdeTask>().configureEach {
    val filterTag = project.group.toString()
    doFirst {
        val pos = PipedOutputStream()
        errorOutput = pos
        thread(isDaemon = true) {
            PipedInputStream(pos).bufferedReader().forEachLine { line ->
                if (filterTag in line) {
                    System.err.println(line)
                }
            }
        }
    }
}
