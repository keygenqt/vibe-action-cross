@file:Suppress("UnstableApiUsage")

import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

rootProject.name = "vibe-action-cross"

/**
 * Enables typesafe project accessors (e.g. projects.shared instead of project(":shared")).
 */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

/**
 * Repositories for resolving Gradle plugins.
 */
pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.3.20"
        id("org.jetbrains.changelog") version "2.5.0"
    }
}

/**
 * Repositories for resolving project dependencies.
 */
dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.jetbrains.intellij.platform.settings") version "2.17.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
    }
}

/**
 * Compose Multiplatform UI modules — shared code, desktop app, and web bundle for VS Code.
 */
include(":plugin-ui:shared")
include(":plugin-ui:resources")
include(":plugin-ui:webApp")
include("plugin-idea")
