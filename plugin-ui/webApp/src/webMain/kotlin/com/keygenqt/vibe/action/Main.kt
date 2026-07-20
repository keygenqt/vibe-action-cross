/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import com.keygenqt.vibe.action.bridge.VsCodeEnvironment

/**
 * VS Code extension entry point (JS target) — mounts the Compose Multiplatform UI
 * into the VS Code webview via ComposeViewport. Standalone browser access (outside
 * the extension's webview) is not supported.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        val isVsCode = isVsCodeWebView()
        val vsCodeApi: dynamic = if (isVsCode) js("acquireVsCodeApi()") else null
        if (isVsCode) {
            InitApp(VsCodeEnvironment(vsCodeApi)) {
                RootAppDispatcher()
            }
        } else {
            MaterialTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Not available outside the VS Code extension.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

/**
 * Checks if running inside a VS Code WebView sandbox.
 */
private fun isVsCodeWebView(): Boolean = js("typeof acquireVsCodeApi !== 'undefined'") as Boolean
