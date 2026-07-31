plugins {
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

/**
 * Installs the required npm dependencies for the VS Code extension.
 * Executes `npm install` in the plugin-vscode directory.
 */
val installDeps = tasks.register<Exec>("installDeps") {
    group = "vscode"
    description = "Installs VS Code extension dependencies"
    workingDir = layout.projectDirectory.dir("plugin-vscode").asFile

    inputs.file(layout.projectDirectory.file("plugin-vscode/package.json"))
    inputs.file(layout.projectDirectory.file("plugin-vscode/package-lock.json"))
    outputs.dir(layout.projectDirectory.dir("plugin-vscode/node_modules"))

    if (System.getProperty("os.name").lowercase().contains("windows")) {
        commandLine("npm.cmd", "install")
    } else {
        commandLine("npm", "install")
    }
}

/**
 * Packages the VS Code extension into a .vsix archive.
 * Ensures that npm dependencies are installed and the Kotlin/JS webApp bundle
 * is built and copied before invoking the vsce packaging tool.
 * The resulting file remains in the plugin-vscode directory.
 */
val packageExtension = tasks.register<Exec>("packageExtension") {
    group = "vscode"
    description = "Packages the VS Code extension (.vsix)"

    dependsOn(installDeps)

    // Build and copy the production JS bundle first
    dependsOn(":plugin-ui:webApp:copyWebAppBundle")
    mustRunAfter(":plugin-ui:webApp:copyWebAppBundle")

    workingDir = layout.projectDirectory.dir("plugin-vscode").asFile

    if (System.getProperty("os.name").lowercase().contains("windows")) {
        commandLine("npm.cmd", "run", "package")
    } else {
        commandLine("npm", "run", "package")
    }
}

/**
 * Full build lifecycle for the VS Code extension.
 * Triggers the packaging process and copies the resulting .vsix artifact
 * to the root dist directory for easy access.
 */
val buildExtension = tasks.register<Copy>("buildExtension") {
    group = "vscode"
    description = "Builds VS Code extension and copies it to dist"

    dependsOn(packageExtension)

    from(layout.projectDirectory.dir("plugin-vscode")) {
        include("*.vsix")
    }
    into(layout.projectDirectory.dir("dist"))
}

/**
 * Builds all plugins (VS Code & IntelliJ) and gathers their artifacts
 * into the root dist directory.
 */
val buildAll = tasks.register<Copy>("buildAll") {
    group = "build"
    description = "Builds all plugins and copies them to dist"

    // Trigger VS Code extension packaging
    dependsOn(buildExtension)
    // Trigger IntelliJ plugin build
    dependsOn(":plugin-idea:buildPlugin")

    // Copy IntelliJ plugin artifact (zip or jar)
    // (VS Code artifact is already copied by buildExtension)
    from(layout.projectDirectory.dir("plugin-idea/build/distributions")) {
        include("*.zip", "*.jar")
    }

    // Gather everything in the root dist folder
    into(layout.projectDirectory.dir("dist"))
}
