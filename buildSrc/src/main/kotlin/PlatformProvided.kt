import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.kotlin.dsl.exclude

/**
 * IntelliJ Platform (via composeUI() + bundledPlugin("org.jetbrains.kotlin"))
 * already ships part of the Compose/AndroidX/coroutines runtime. If our own
 * dependencies transitively pull in the same artifacts, we end up with two
 * copies of the same type loaded by different PluginClassLoaders, causing
 * LinkageError/ClassCastException.
 *
 * This is the single place where such coordinates are listed — when new
 * conflicts show up, add them here rather than at the call site.
 */

/**
 * Bare Compose runtime — what composeUI() actually provides.
 */
fun ExternalModuleDependency.excludeComposeRuntime() {
    exclude(group = "org.jetbrains.compose.runtime")
    exclude(group = "org.jetbrains.compose.ui")
    exclude(group = "org.jetbrains.compose.foundation")
    exclude(group = "org.jetbrains.compose.animation")
    exclude(group = "androidx.compose.runtime")
}

/**
 * Compose runtime + the Navigation3 ecosystem (lifecycle/savedstate/navigationevent),
 * which the IDE also provides — for dependencies like material3/koin-compose.
 */
fun ExternalModuleDependency.excludePlatformProvided() {
    excludeComposeRuntime()
    exclude(group = "androidx.lifecycle")
    exclude(group = "org.jetbrains.androidx.lifecycle")
    exclude(group = "androidx.savedstate")
    exclude(group = "org.jetbrains.androidx.savedstate")
    exclude(group = "androidx.navigationevent")
    exclude(group = "org.jetbrains.androidx.navigationevent")
}
