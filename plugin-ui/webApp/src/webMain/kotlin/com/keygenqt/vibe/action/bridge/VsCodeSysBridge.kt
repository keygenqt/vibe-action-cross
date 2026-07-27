/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import kotlinx.browser.window
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

/**
 * VS Code implementation of the system bridge, delegating notifications and dialogs
 * to the extension host through VsCodeApi.
 */
class VsCodeSysBridge(private val api: VsCodeApi) : SysBridge {

    /**
     * Single-slot callbacks for the currently running CLI process.
     * The extension host supports concurrent processes (targets are passed per call),
     * but this bridge does not: a second runCli would silently overwrite the first
     * call's callbacks, leaving its coroutine hanging. Guarded below.
     */
    private var cliEventCallback: ((String) -> Unit)? = null
    private var cliDoneCallback: ((Int) -> Unit)? = null

    init {
        // Listen for CLI events from extension host
        window.addEventListener("message", { event ->
            val data = event.asDynamic().data
            when (data?.target) {
                "cliEvent" -> cliEventCallback?.invoke(data.args[0] as String)
                "cliDone" -> cliDoneCallback?.invoke(data.args[0] as Int)
            }
        })
    }

    /**
     * Shows a native VS Code toast via vscode.window.showInformationMessage.
     */
    override val showNotification: ((title: String, message: String) -> Unit) = { title, message ->
        api.send("showInformationMessage", arrayOf("$title\n\n$message"))
    }

    /**
     * Shows a modal Yes/No confirm dialog via vscode.window.showWarningMessage.
     */
    override val showConfirmDialog: ((title: String, message: String, onResult: (Boolean) -> Unit) -> Unit) =
        { title, message, onResult ->
            api.send(
                target = "showInformationMessage",
                args = arrayOf(
                    "$title\n\n$message",
                    kotlin.js.json(Pair("modal", true)),
                    "Yes",
                    "No",
                ),
            ) { result -> onResult(result == "Yes") }
        }

    /**
     * Listens for a "themeChanged" broadcast pushed from the extension host
     * (subscribed to vscode.window.onDidChangeActiveColorTheme on that side).
     */
    override val onThemeChanged: ((onChanged: () -> Unit) -> (() -> Unit)) = { onChanged ->
        val listener: (Event) -> Unit = { event ->
            val data = event.asDynamic().data
            if (data?.target == "themeChanged") {
                onChanged()
            }
        }
        window.addEventListener("message", listener)
        val unsubscribe: () -> Unit = { window.removeEventListener("message", listener) }
        unsubscribe
    }

    /**
     * Runs vibe-action CLI via extension host child_process.spawn.
     * Events stream back through postMessage with targets "cliEvent"/"cliDone".
     *
     * Throws [IllegalStateException] if another CLI process is already running —
     * concurrent calls are not supported (callbacks would be overwritten).
     */
    override val runCli: ((args: List<String>, onEvent: (String) -> Unit, onDone: (Int) -> Unit) -> Unit) =
        { args, onEvent, onDone ->
            check(cliDoneCallback == null) { "Concurrent runCli is not supported by VsCodeSysBridge" }
            cliEventCallback = onEvent
            cliDoneCallback = { code ->
                // Clear slots before notifying — the bridge is reusable immediately after.
                cliEventCallback = null
                cliDoneCallback = null
                onDone(code)
            }
            api.send("runCli", arrayOf(args.toTypedArray(), "cliEvent", "cliDone"))
        }

    /**
     * Opens a file in VS Code editor via vscode.window.showTextDocument.
     * Extension host converts path to vscode.Uri and opens it.
     */
    override val openFile: ((path: String) -> Unit) = { path ->
        api.send("openFile", arrayOf(path))
    }

    /**
     * Checks if a file exists at the given path via bridge API.
     * Times out if the extension host never responds (its callback would
     * otherwise leak in VsCodeApi's pending map forever).
     */
    override val fileExists: (suspend (String) -> Boolean)? = { path ->
        try {
            withTimeout(FILE_EXISTS_TIMEOUT) {
                suspendCancellableCoroutine { cont ->
                    api.send(
                        target = "fileExists",
                        args = arrayOf(path)
                    ) { result -> cont.resume(result as? Boolean ?: false) }
                }
            }
        } catch (e: TimeoutCancellationException) {
            // The orphaned callback remains in VsCodeApi until (if ever) the host
            // responds — acceptable, the watchdog in VsCodeApi will report a leak.
            false
        }
    }

    /**
     * Retrieves the currently selected text via the API and passes it to the provided callback.
     */
    override val getSelectedText: (((String?) -> Unit) -> Unit) = { onResult ->
        api.send("getSelectedText", EMPTY_ARGS) { result ->
            onResult(result as? String)
        }
    }

    /**
     * Replaces the currently selected text with the given newText via the API.
     */
    override val replaceSelectedText: ((String) -> Unit) = { newText ->
        api.send("replaceSelectedText", arrayOf(newText))
    }

    private companion object {
        val FILE_EXISTS_TIMEOUT = 5.seconds
        val EMPTY_ARGS: Array<Any?> = emptyArray()
    }
}
