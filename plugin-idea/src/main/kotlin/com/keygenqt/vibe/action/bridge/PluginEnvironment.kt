/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import com.intellij.openapi.project.Project
import com.keygenqt.vibe.action.bridge.view.PluginAboutViewBridge
import com.keygenqt.vibe.action.bridge.view.PluginHistoryDetailViewBridge
import com.keygenqt.vibe.action.bridge.view.PluginHistoryViewBridge
import com.keygenqt.vibe.action.bridge.view.PluginMainViewBridge
import com.keygenqt.vibe.action.bridge.view.PluginSettingsViewBridge

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
            PlatformView.History to PluginHistoryViewBridge(),
            PlatformView.HistoryDetail to PluginHistoryDetailViewBridge(),
            PlatformView.About to PluginAboutViewBridge(),
        ),
    ),
) : Environment
