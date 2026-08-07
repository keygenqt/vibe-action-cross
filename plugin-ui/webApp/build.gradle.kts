plugins {
    // buildSrc precompiled script plugin, not a Plugin Portal id
    id("spotless")
    // Version from libs.versions.toml
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
 * Finds the VS Code `code` CLI.
 * Gradle started from IDEA gets a stripped-down PATH, so plain "code" is not found there.
 */
fun resolveVscodeBinary(): String {
    // 1. Explicit overrides
    providers.gradleProperty("vscodePath").orNull?.let { return it }
    providers.environmentVariable("VSCODE_BIN").orNull?.let { return it }

    // 2. PATH lookup (works for terminal runs)
    providers.environmentVariable("PATH").orNull
        ?.split(File.pathSeparatorChar)
        ?.map { File(it, "code") }
        ?.firstOrNull { it.isFile && it.canExecute() }
        ?.let { return it.absolutePath }

    // 3. Well-known locations
    listOf(
        "/usr/local/bin/code",        // macOS: "Install 'code' command in PATH"
        "/opt/homebrew/bin/code",     // Apple Silicon + Homebrew
        "/Applications/Visual Studio Code.app/Contents/Resources/app/bin/code",
        "/usr/bin/code",              // Linux
        "/snap/bin/code",
    ).map(::File).firstOrNull { it.isFile }?.let { return it.absolutePath }

    error(
        "VS Code CLI 'code' not found. " +
                "Pass -PvscodePath=/path/to/code or set the VSCODE_BIN env var."
    )
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
        resolveVscodeBinary(),
        "--extensionDevelopmentPath=${dir.resolve("plugin-vscode")}",
        "--new-window"
    )
}

/**
 * Copies the webApp JS dev bundle into plugin-vscode for fast iteration —
 * unminified, no tree-shaking, but builds noticeably faster than production.
 */
tasks.register<Copy>("copyWebAppBundleDev") {
    group = "vscode"
    description = "Copy webApp JS development bundle into plugin-vscode"
    val dir = rootProject.projectDir

    dependsOn(":plugin-ui:webApp:jsBrowserDevelopmentExecutableDistribution")

    from(dir.resolve("plugin-ui/webApp/build/dist/js/developmentExecutable"))
    into(dir.resolve("plugin-vscode/productionExecutable"))
}

/**
 * Builds the webApp bundle in development mode (fast, unminified) and launches VS Code.
 */
tasks.register<Exec>("launchVscodeDev") {
    group = "vscode"
    description = "Build webApp JS bundle (dev mode) and launch VS Code extension"
    val dir = rootProject.projectDir

    dependsOn("copyWebAppBundleDev")

    commandLine(
        resolveVscodeBinary(),
        "--extensionDevelopmentPath=${dir.resolve("plugin-vscode")}",
        "--new-window"
    )
}
