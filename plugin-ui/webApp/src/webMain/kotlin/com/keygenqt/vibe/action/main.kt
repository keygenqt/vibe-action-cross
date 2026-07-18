package com.keygenqt.vibe.action

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeViewport
import com.keygenqt.vibe.action.bridge.VsCodeEnvironment
import kotlinx.browser.document
import kotlinx.browser.window
import androidx.compose.material3.Text

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
                VSExtensionTheme {
                    RootAppDispatcher()
                }
            }
        } else {
            MaterialTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Not available outside the VS Code extension.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

/**
 * Builds a Material 3 color scheme from VS Code CSS custom properties for native look and feel.
 */
@Composable
private fun VSExtensionTheme(content: @Composable () -> Unit) {
    val rootElement = document.documentElement ?: return
    val computedStyle = window.getComputedStyle(rootElement)

    // Resolves a CSS color token or falls back to a default value
    val getThemeColor = { key: String, fallback: String ->
        val rawColor = computedStyle.getPropertyValue(key).trim()
        Color(parseHexColor(rawColor.ifBlank { fallback }))
    }

    // Dynamic color palette mapped from VS Code theme tokens
    val vsCodeDynamicPalette = darkColorScheme(
        background = getThemeColor("--vscode-editorWidget-background", "#252526"),
        surface = getThemeColor("--vscode-editorWidget-background", "#252526"),
        surfaceContainer = getThemeColor("--vscode-input-background", "#3c3c3c"),
        surfaceVariant = getThemeColor("--vscode-input-background", "#3c3c3c"),
        primary = getThemeColor("--vscode-focusBorder", "#007fd4"),
        primaryContainer = getThemeColor("--vscode-focusBorder", "#007fd4"),
        onBackground = getThemeColor("--vscode-foreground", "#cccccc"),
        onSurface = getThemeColor("--vscode-foreground", "#cccccc"),
        onPrimary = getThemeColor("--vscode-strongForeground", "#ffffff")
    )

    // Apply the dynamic palette
    MaterialTheme(colorScheme = vsCodeDynamicPalette) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}

/**
 * Checks if running inside a VS Code WebView sandbox.
 */
private fun isVsCodeWebView(): Boolean {
    return js("typeof acquireVsCodeApi !== 'undefined'") as Boolean
}

/**
 * Parses a hex color string (3, 6, or 8 chars) into an ARGB Long value.
 */
private fun parseHexColor(hex: String): Long {
    val cleanHex = hex.trim().removePrefix("#")
    return when (cleanHex.length) {
        6 -> "FF$cleanHex".toLong(16)
        8 -> cleanHex.toLong(16)
        else -> 0xFF1E1E1E
    }
}
