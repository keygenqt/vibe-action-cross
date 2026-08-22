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
     * Returns language code from API.
     */
    override val language: String?
        get() = api.language

    /**
     * Callbacks of live CLI invocations, keyed by invocation id.
     * Each runCli call gets a unique id; the extension host tags events with
     * per-invocation targets ("cliEvent:<id>"/"cliDone:<id>"), so events of
     * a superseded process can never reach a newer invocation's callbacks.
     */
    private data class CliInvocation(
        val onEvent: (String, (String) -> Unit) -> Unit,
        val writeStdin: (String) -> Unit,
        val onDone: (Int, Pair<String, String>) -> Unit,
    )

    private val cliCallbacks = mutableMapOf<String, CliInvocation>()
    private var cliSeq = 0

    private val cliEventListener: (Event) -> Unit = listener@{ event ->
        val data = event.asDynamic().data
        val target = data?.target as? String ?: return@listener
        when {
            target.startsWith(CLI_EVENT_TARGET) -> {
                val id = target.removePrefix(CLI_EVENT_TARGET)
                cliCallbacks[id]?.let { inv ->
                    inv.onEvent(data.args[0] as String, inv.writeStdin)
                }
            }

            target.startsWith(CLI_DONE_TARGET) -> {
                val id = target.removePrefix(CLI_DONE_TARGET)
                val exitCode = data.args[0] as Int
                val streamsDynamic = data.args[1]
                val streams = Pair(
                    streamsDynamic.first as String,
                    streamsDynamic.second as String,
                )
                cliCallbacks.remove(id)?.onDone?.invoke(exitCode, streams)
            }
        }
    }

    init {
        window.addEventListener("message", cliEventListener)
    }

    /**
     * Removes the global message listener and drops pending callbacks.
     * Call when the owning composition leaves (DisposableEffect.onDispose).
     */
    fun dispose() {
        window.removeEventListener("message", cliEventListener)
        cliCallbacks.clear()
        api.dispose()
    }

    /**
     * Shows a native VS Code toast via vscode.window.showInformationMessage.
     */
    override val showNotification: ((title: String, message: String) -> Unit) = { title, message ->
        api.send("showInformationMessage", arrayOf("$title — $message"))
    }

    /**
     * Shows a modal Yes/No confirm dialog via vscode.window.showInformationMessage.
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
     * Events stream back through postMessage with per-invocation targets
     * ("cliEvent:<id>"/"cliDone:<id>"), so concurrent calls never cross-talk.
     *
     * onEvent receives a stdin writer alongside each line — the writer
     * is available from the first event, eliminating the race between
     * event delivery and process handle assignment.
     *
     * [CliProcess.cancel] drops the callback slot and asks the extension host
     * to kill the child process ("killCli"); the resulting late "cliDone:<id>"
     * finds no slot and is ignored.
     */
    override val runCli: ((args: List<String>, onEvent: (String, (String) -> Unit) -> Unit, onDone: (Int, Pair<String, String>) -> Unit) -> CliProcess) =
        { args, onEvent, onDone ->
            val id = (cliSeq++).toString()
            val writeStdin: (String) -> Unit = { text ->
                api.send("writeStdin", arrayOf(id, text))
            }
            cliCallbacks[id] = CliInvocation(onEvent, writeStdin, onDone)
            api.send(
                "runCli",
                arrayOf(args.toTypedArray(), "$CLI_EVENT_TARGET$id", "$CLI_DONE_TARGET$id", id),
            )
            object : CliProcess {
                override fun cancel() {
                    // Drop the slot first — the late cliDone is then ignored.
                    if (cliCallbacks.remove(id) != null) {
                        api.send("killCli", arrayOf(id))
                    }
                }
                override fun writeStdin(text: String) = writeStdin(text)
            }
        }

    /**
     * Opens a file in VS Code editor via vscode.window.showTextDocument.
     * Extension host converts path to vscode.Uri and opens it.
     */
    override val openFile: ((path: String) -> Unit) = { path ->
        api.send("openFile", arrayOf(path))
    }

    /**
     * Opens a URL in the system browser via vscode.env.openExternal.
     * The extension host converts the URL to vscode.Uri and opens it.
     */
    override val openUrl: ((url: String) -> Unit) = { url ->
        api.send("openUrl", arrayOf(url))
    }

    /**
     * Writes content to a file via bridge API.
     */
    override val writeFile: (suspend (String, String) -> Unit) = { path, content ->
        try {
            withTimeout(FILE_EXISTS_TIMEOUT) {
                suspendCancellableCoroutine { cont ->
                    api.send(
                        target = "writeFile",
                        args = arrayOf(path, content),
                    ) { cont.resume(Unit) }
                }
            }
        } catch (_: TimeoutCancellationException) {
            // Ignore timeout, fire-and-forget behavior is acceptable here
        }
    }

    /**
     * Deletes a file at the given path via bridge API.
     * Times out if the extension host never responds.
     */
    override val deleteFile: (suspend (String) -> Boolean) = { path ->
        try {
            withTimeout(FILE_EXISTS_TIMEOUT) {
                // Reusing the same timeout duration
                suspendCancellableCoroutine { cont ->
                    api.send(
                        target = "deleteFile",
                        args = arrayOf(path),
                    ) { result -> cont.resume(result as? Boolean ?: false) }
                }
            }
        } catch (_: TimeoutCancellationException) {
            false
        }
    }

    /**
     * Checks if a file exists at the given path via bridge API.
     * Times out if the extension host never responds (its callback would
     * otherwise leak in VsCodeApi's pending map forever).
     */
    override val fileExists: (suspend (String) -> Boolean) = { path ->
        try {
            withTimeout(FILE_EXISTS_TIMEOUT) {
                suspendCancellableCoroutine { cont ->
                    api.send(
                        target = "fileExists",
                        args = arrayOf(path),
                    ) { result -> cont.resume(result as? Boolean ?: false) }
                }
            }
        } catch (_: TimeoutCancellationException) {
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

    /**
     * Prompts the user for text input via a dialog and passes it to the provided handler.
     */
    override val getDialogText: (((String?) -> Unit) -> Unit) = { onResult ->
        api.send("getDialogText", EMPTY_ARGS) { res ->
            onResult(res as String?)
        }
    }

    /**
     * Retrieves the path of the currently active file in the editor.
     */
    override val getCurrentFilePath: (((String?) -> Unit) -> Unit) = { onResult ->
        api.send("getCurrentFilePath", EMPTY_ARGS) { result ->
            onResult(result as? String)
        }
    }

    /**
     * Retrieves the root path of the current project/workspace.
     */
    override val getProjectPath: (((String?) -> Unit) -> Unit) = { onResult ->
        api.send("getProjectPath", EMPTY_ARGS) { result ->
            onResult(result as? String)
        }
    }

    /**
     * Retrieves the current cursor line number (1-indexed) in the active editor.
     */
    override val getCursorLine: (((String?) -> Unit) -> Unit) = { onResult ->
        api.send("getCursorLine", EMPTY_ARGS) { result ->
            onResult(result as? String)
        }
    }

    /**
     * Retrieves the current text from the system clipboard via the API.
     */
    override val getClipboardText: (((String?) -> Unit) -> Unit) = { onResult ->
        api.send("getClipboardText", EMPTY_ARGS) { result ->
            onResult(result as? String)
        }
    }

    /**
     * Writes the given text to the system clipboard via the API.
     */
    override val setClipboardText: ((String) -> Unit) = { newText ->
        api.send("setClipboardText", arrayOf(newText))
    }

    /**
     * Callback that shows a multiline text dialog/output to the user.
     */
    override val showTextDialog: ((title: String, text: String) -> Unit) = { title, text ->
        api.send("showTextDialog", arrayOf(title, text))
    }

    /**
     * Saves a simple string preference to VS Code globalState via bridge API.
     */
    override val savePreference: ((String, String) -> Unit) = { key, value ->
        api.send("savePreference", arrayOf(key, value))
    }

    /**
     * Loads a simple string preference from VS Code globalState via bridge API.
     * Times out if the extension host never responds.
     */
    override val loadPreference: (suspend (String) -> String?) = { key ->
        try {
            withTimeout(FILE_EXISTS_TIMEOUT) {
                suspendCancellableCoroutine { cont ->
                    api.send(
                        target = "loadPreference",
                        args = arrayOf(key),
                    ) { result -> cont.resume(result as? String) }
                }
            }
        } catch (_: TimeoutCancellationException) {
            null
        }
    }

    private companion object {
        const val CLI_EVENT_TARGET = "cliEvent:"
        const val CLI_DONE_TARGET = "cliDone:"

        val FILE_EXISTS_TIMEOUT = 5.seconds
        val EMPTY_ARGS: Array<Any?> = emptyArray()
    }
}
