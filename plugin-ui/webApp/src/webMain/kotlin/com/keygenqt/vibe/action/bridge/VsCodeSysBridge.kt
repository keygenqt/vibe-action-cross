package com.keygenqt.vibe.action.bridge

import com.keygenqt.vibe.action.bridge.SysBridge

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
                    "No"
                )
            ) { result -> onResult(result == "Yes") }
        }
}
