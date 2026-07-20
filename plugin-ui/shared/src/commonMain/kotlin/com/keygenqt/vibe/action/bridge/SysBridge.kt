/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

/**
 * Low-level operational core bridge mapping to host OS and IDE hooks.
 * Provides platform-agnostic access to notifications and modal dialogs.
 */
interface SysBridge {
    /**
     * Dispatches a native toast notification to the active host system.
     */
    val showNotification: ((title: String, message: String) -> Unit)?

    /**
     * Invokes a modal confirmation dialog on the target platform.
     */
    val showConfirmDialog: ((title: String, message: String, onResult: (Boolean) -> Unit) -> Unit)?

    /**
     * Subscribes to host theme changes. Returns an unsubscribe function.
     * Platforms where recomposition already reacts to theme changes on its
     * own (e.g. IDEA via Jewel) can leave this null.
     */
    val onThemeChanged: ((onChanged: () -> Unit) -> (() -> Unit))?
}

/**
 * No-op [SysBridge] implementation for platforms without native system integrations.
 */
open class SysBridgeEmpty : SysBridge {
    override val showNotification: ((title: String, message: String) -> Unit)? = null
    override val showConfirmDialog: ((title: String, message: String, onResult: (Boolean) -> Unit) -> Unit)? = null
    override val onThemeChanged: ((onChanged: () -> Unit) -> (() -> Unit))? = null
}
