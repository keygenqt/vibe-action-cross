/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

/**
 * Handle to a CLI process started via [SysBridge.runCli].
 */
interface CliProcess {
    /**
     * Requests termination of the process. Idempotent and non-blocking.
     *
     * After this call the bridge drops all further events for this
     * invocation: neither onEvent nor onDone will be delivered (events
     * already racing on another thread may slip through — callers must
     * tolerate that, see CommandProvider's isActive guards).
     */
    fun cancel()
}

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
     *
     * Concurrent calls ARE supported: every invocation owns an independent
     * event stream — events of an older process are never delivered to a
     * newer invocation's callbacks. Starting a new process supersedes the
     * previous one (the Rust RunGuard terminates it; the bridge may also
     * destroy it directly).
     *
     * Returns a [CliProcess] handle for terminating the process.
     * Unless cancel() is called, onDone is invoked exactly once when the
     * process exits. A process terminated because a newer CLI instance
     * superseded it exits with code 130 (see RunGuard::EXIT_SUPERSEDED).
     */
    val runCli: ((args: List<String>, onEvent: (String) -> Unit, onDone: (Int) -> Unit) -> CliProcess)?

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
     * Writes the given string to the system clipboard.
     */
    val setClipboardText: ((String) -> Unit)?

    /**
     * Callback that replaces the currently selected text with the given string.
     */
    val replaceSelectedText: ((String) -> Unit)?

    /**
     * Callback that shows a multiline text dialog/output to the user.
     */
    val showTextDialog: ((title: String, text: String) -> Unit)?
}
