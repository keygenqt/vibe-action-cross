package com.keygenqt.vibe.action.bridge

import com.keygenqt.vibe.action.bridge.view.VsCodeMainViewBridge
import com.keygenqt.vibe.action.bridge.view.VsCodeSettingsViewBridge

/**
 * VS Code implementation of the runtime environment.
 * Wires the system bridge and view bridges for VS Code WebView mode.
 */
data class VsCodeEnvironment(
    /**
     * VS Code API instance acquired via acquireVsCodeApi().
     */
    val vsCodeApi: dynamic,

    /**
     * Sets the runtime platform to VS Code Extension.
     */
    override val platform: Platform = Platform.VSExtension,

    /**
     * Provides the bridge with VS Code system hooks and Main/Settings view bridges.
     */
    override val bridge: Bridge = Bridge(
        res = ComposeResBridge(),
        sys = VsCodeSysBridge(VsCodeApi(vsCodeApi)),
        vws = hashMapOf(
            PlatformView.Main to VsCodeMainViewBridge(),
            PlatformView.Settings to VsCodeSettingsViewBridge(),
        )
    )
) : Environment
