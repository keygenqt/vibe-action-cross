/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

/**
 * Supported platform view contexts for modular separation of UI layouts.
 */
enum class PlatformView {
    Main,
    Settings,
    History,
    HistoryDetail,
    About,
}

/**
 * Returns the ViewBridge for this PlatformView from the environment.
 */
fun PlatformView.bridge(env: Environment): ViewBridge = env.bridge.vws[this] ?: error("No ViewBridge registered for $this")
