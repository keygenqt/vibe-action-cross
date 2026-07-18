package com.keygenqt.vibe.action.bridge

import com.intellij.openapi.project.Project
import com.keygenqt.vibe.action.bridge.view.PluginMainViewBridge
import com.keygenqt.vibe.action.bridge.view.PluginSettingsViewBridge
import com.keygenqt.vibe.action.bridge.Bridge
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.Platform
import com.keygenqt.vibe.action.bridge.PlatformView

/**
 * IntelliJ plugin implementation of the runtime environment.
 * Wires the system bridge with IDE-specific hooks and view bridges for Main/Settings.
 */
data class PluginEnvironment(
    /**
     * Active IntelliJ project instance for IDE API access.
     */
    val project: Project,

    /**
     * Sets the runtime platform to IntelliJ Plugin.
     */
    override val platform: Platform = Platform.IntellJPlugin,

    /**
     * Provides the bridge with plugin system hooks and Main/Settings view bridges.
     */
    override val bridge: Bridge = Bridge(
        res = PluginResBridge(),
        sys = PluginSysBridge(project),
        vws = hashMapOf(
            PlatformView.Main to PluginMainViewBridge(),
            PlatformView.Settings to PluginSettingsViewBridge(),
        )
    )
) : Environment
