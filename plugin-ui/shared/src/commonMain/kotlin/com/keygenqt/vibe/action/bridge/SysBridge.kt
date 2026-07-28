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

    /**
     * Runs vibe-action CLI with given arguments.
     * Streams NDJSON lines to onEvent, exit code to onDone.
     */
    val runCli: ((args: List<String>, onEvent: (String) -> Unit, onDone: (Int) -> Unit) -> Unit)?

    /**
     * Opens a file in the platform's native editor/viewer.
     */
    val openFile: ((path: String) -> Unit)?

    /**
     * Checks whether a file exists at the given absolute path.
     * Suspends until the result is available. On platforms with synchronous
     * I/O the call returns immediately.
     */
    val fileExists: (suspend (String) -> Boolean)?

    /**
     * Retrieves the currently selected text and passes it to the provided handler.
     */
    val getSelectedText: (((String?) -> Unit) -> Unit)?

    /**
     * Retrieves the current text from the system clipboard.
     */
    val getClipboardText: (((String?) -> Unit) -> Unit)?

    /**
     * Callback that replaces the currently selected text with the given string.
     */
    val replaceSelectedText: ((String) -> Unit)?
}
