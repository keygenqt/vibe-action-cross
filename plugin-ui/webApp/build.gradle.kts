plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            // Shared Compose UI code and platform resources
            implementation(projects.pluginUi.resources)
            implementation(projects.pluginUi.shared)

            // Material3 theming
            implementation(libs.compose.material3)

            // Dependency injection
            implementation(libs.koin.compose)

            // Navigation between screens
            implementation(libs.navigation3.ui)
        }
    }
}

/**
 * Copies the webApp JS bundle into plugin-vscode for VS Code extension loading.
 */
tasks.register<Copy>("copyWebAppBundle") {
    group = "vscode"
    description = "Copy webApp JS bundle into plugin-vscode"
    val dir = rootProject.projectDir

    dependsOn(":plugin-ui:webApp:jsBrowserDistribution")

    from(dir.resolve("plugin-ui/webApp/build/dist/js/productionExecutable"))
    into(dir.resolve("plugin-vscode/productionExecutable"))
}

/**
 * Builds the webApp bundle and launches VS Code with the extension in development mode.
 */
tasks.register<Exec>("launchVscode") {
    group = "vscode"
    description = "Build webApp JS bundle and launch VS Code extension in development mode"
    val dir = rootProject.projectDir

    dependsOn("copyWebAppBundle")

    commandLine(
        "code",
        "--extensionDevelopmentPath=${dir.resolve("plugin-vscode")}",
        "--new-window"
    )
}
