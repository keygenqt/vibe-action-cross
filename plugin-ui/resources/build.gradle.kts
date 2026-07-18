plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    // Enables the standard source set hierarchy
    applyDefaultHierarchyTemplate()

    jvm()
    js { browser() }

    sourceSets {
        commonMain.dependencies {
            // Painter/Composable types needed to expose resources as Compose UI primitives
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            // PlatformIcon/PlatformImage/PlatformString enums that ComposeResBridge implements against
            implementation(projects.pluginUi.shared)
        }
    }
}
