plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(plugin(libs.plugins.spotless))
}

/**
 * Turns a version-catalog plugin alias into a classpath dependency notation,
 * so it can be used in buildSrc instead of only via plugins {}.
 */
fun plugin(dep: Provider<PluginDependency>) =
    dep.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
