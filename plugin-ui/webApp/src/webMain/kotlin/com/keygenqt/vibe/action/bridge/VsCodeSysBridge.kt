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
}
