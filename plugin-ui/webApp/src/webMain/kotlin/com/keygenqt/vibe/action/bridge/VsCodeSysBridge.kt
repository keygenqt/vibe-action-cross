/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * VS Code implementation of the system bridge, delegating notifications and dialogs
 * to the extension host through VsCodeApi.
 */
class VsCodeSysBridge(private val api: VsCodeApi) : SysBridge {

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
     */
    override val runCli: ((args: List<String>, onEvent: (String) -> Unit, onDone: (Int) -> Unit) -> Unit) =
        { args, onEvent, onDone ->
            cliEventCallback = onEvent
            cliDoneCallback = onDone
            api.send("runCli", arrayOf(args.toTypedArray(), "cliEvent", "cliDone"))
        }

    /**
     * Opens a file in VS Code editor via vscode.window.showTextDocument.
     * Extension host converts path to vscode.Uri and opens it.
     */
    override val openFile: ((path: String) -> Unit) = { path ->
        api.send("openFile", arrayOf(path))
    }
}
