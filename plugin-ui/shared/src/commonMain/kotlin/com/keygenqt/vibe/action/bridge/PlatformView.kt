package com.keygenqt.vibe.action.bridge

/**
 * Supported platform view contexts for modular separation of UI layouts.
 */
enum class PlatformView {
    Main,
    Settings,
}

/**
 * Returns the ViewBridge for this PlatformView from the environment.
 */
fun PlatformView.bridge(env: Environment): ViewBridge {
    return env.bridge.vws[this] ?: error("No ViewBridge registered for $this")
}
