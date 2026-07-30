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
     * Callbacks of live CLI invocations, keyed by invocation id.
     * Each runCli call gets a unique id; the extension host tags events with
     * per-invocation targets ("cliEvent:<id>"/"cliDone:<id>"), so events of
     * a superseded process can never reach a newer invocation's callbacks.
     */
    private val cliCallbacks = mutableMapOf<String, Pair<(String) -> Unit, (Int) -> Unit>>()
    private var cliSeq = 0

    init {
        // Route CLI events from the extension host to the owning invocation.
        window.addEventListener("message", { event ->
            val data = event.asDynamic().data
            val target = data?.target as? String ?: return@addEventListener
            when {
                target.startsWith(CLI_EVENT_TARGET) ->
                    cliCallbacks[target.removePrefix(CLI_EVENT_TARGET)]
                        ?.first?.invoke(data.args[0] as String)

                target.startsWith(CLI_DONE_TARGET) -> {
                    val id = target.removePrefix(CLI_DONE_TARGET)
                    // Remove before notifying — the slot is freed even if onDone throws.
                    cliCallbacks.remove(id)?.second?.invoke(data.args[0] as Int)
                }
            }
        })
    }

    /**
     * Shows a native VS Code toast via vscode.window.showInformationMessage.
     */
    override val showNotification: ((title: String, message: String) -> Unit) = { title, message ->
        val t = if (title.endsWith(".")) title else "$title."
        api.send("showInformationMessage", arrayOf("$t\n\n$message"))
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
     * Events stream back through postMessage with per-invocation targets
     * ("cliEvent:<id>"/"cliDone:<id>"), so concurrent calls never cross-talk.
     *
     * [CliProcess.cancel] drops the callback slot and asks the extension host
     * to kill the child process ("killCli"); the resulting late "cliDone:<id>"
     * finds no slot and is ignored.
     */
    override val runCli: ((args: List<String>, onEvent: (String) -> Unit, onDone: (Int) -> Unit) -> CliProcess) =
        { args, onEvent, onDone ->
            val id = (cliSeq++).toString()
            cliCallbacks[id] = onEvent to onDone
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
                        args = arrayOf(path),
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

    private companion object {
        const val CLI_EVENT_TARGET = "cliEvent:"
        const val CLI_DONE_TARGET = "cliDone:"

        val FILE_EXISTS_TIMEOUT = 5.seconds
        val EMPTY_ARGS: Array<Any?> = emptyArray()
    }
}
