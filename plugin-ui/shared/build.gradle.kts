plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    compilerOptions {
        // Common compiler options applied to all Kotlin source sets
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm()
    js { browser() }

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform — compileOnly, actually provided by IntelliJ Platform via composeUI()
            compileOnly(libs.compose.runtime)
            compileOnly(libs.compose.foundation)
            compileOnly(libs.compose.material3)
            compileOnly(libs.compose.ui)
            compileOnly(libs.compose.components.resources)
            compileOnly(libs.compose.uiToolingPreview)
            compileOnly(libs.navigation3.ui)
            compileOnly(libs.navigationevent.compose)

            // androidx.lifecycle.*Compose is NOT provided by the IDE (unlike the above),
            // so it must be bundled. Declared in the dependencies{} block at the bottom
            // of this file instead of here: KotlinDependencyHandler (this DSL) has no
            // overload that accepts an exclude() lambda for a version-catalog Provider —
            // only the raw Gradle DependencyHandler (by configuration name) supports that.

            // HTTP client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.logging)

            // Dependency injection
            implementation(libs.koin.core)
            compileOnly(libs.koin.compose)

            // Logging
            implementation(libs.kermit)
            implementation(libs.kermit.koin)

            // Date/time
            implementation(libs.kotlinx.datetime)
        }

        jvmMain.dependencies {
            // Jewel theme for IntelliJ — compileOnly, provided by IDE
            compileOnly(libs.jewel.int.ui.standalone)
            // Ktor OkHttp engine for JVM
            implementation(libs.ktor.client.okhttp)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jsMain.dependencies {
            // Compose runtime for JS target
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)

            // Browser API wrappers
            implementation(libs.wrappers.browser)

            // Ktor JS engine
            implementation(libs.ktor.client.js)
        }
    }
}

// See comment in commonMain.dependencies above.
// excludeComposeRuntime() comes from buildSrc/src/main/kotlin/PlatformProvided.kt
dependencies {
    "commonMainImplementation"(libs.androidx.lifecycle.viewmodelCompose) { excludeComposeRuntime() }
    "commonMainImplementation"(libs.androidx.lifecycle.runtimeCompose) { excludeComposeRuntime() }
}
