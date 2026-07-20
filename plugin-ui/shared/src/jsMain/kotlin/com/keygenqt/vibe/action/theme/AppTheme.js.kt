/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.css.CSSStyleDeclaration

/**
 * Recomposition trigger — its value is never read for content, only
 * subscribed to. Incrementing it forces platformColorScheme() to re-run
 * and re-read the live theme colors after a host theme change.
 */
private val themeVersion = mutableStateOf(0)

@Composable
actual fun platformColorScheme(): ColorScheme {
    themeVersion.value

    val rootElement = document.documentElement ?: return DarkColorScheme
    val style = window.getComputedStyle(rootElement)
    val isDark = document.body?.classList?.contains("vscode-dark") != false
    val base = if (isDark) DarkColorScheme else LightColorScheme

    val sys = ViewEnvironment.current.bridge.sys
    DisposableEffect(Unit) {
        val unsubscribe = sys.onThemeChanged?.invoke { themeVersion.value++ }
        onDispose { unsubscribe?.invoke() }
    }

    return base.copy(
        primary = getThemeColor(style, "--vscode-button-background", base.primary),
        onPrimary = getThemeColor(style, "--vscode-button-foreground", base.onPrimary),
        primaryContainer = getThemeColor(style, "--vscode-button-hoverBackground", base.primaryContainer),
        onPrimaryContainer = getThemeColor(style, "--vscode-button-foreground", base.onPrimaryContainer),
        secondary = getThemeColor(style, "--vscode-button-secondaryBackground", base.secondary),
        onSecondary = getThemeColor(style, "--vscode-button-secondaryForeground", base.onSecondary),
        secondaryContainer = getThemeColor(style, "--vscode-list-inactiveSelectionBackground", base.secondaryContainer),
        onSecondaryContainer = getThemeColor(style, "--vscode-foreground", base.onSecondaryContainer),
        tertiary = getThemeColor(style, "--vscode-charts-purple", base.tertiary),
        onTertiary = getThemeColor(style, "--vscode-sideBar-background", base.onTertiary),
        tertiaryContainer = getThemeColor(style, "--vscode-editorWidget-background", base.tertiaryContainer),
        onTertiaryContainer = getThemeColor(style, "--vscode-foreground", base.onTertiaryContainer),
        error = getThemeColor(style, "--vscode-editorError-foreground", base.error),
        onError = getThemeColor(style, "--vscode-statusBarItem-errorForeground", base.onError),
        errorContainer = getThemeColor(style, "--vscode-inputValidation-errorBackground", base.errorContainer),
        onErrorContainer = getThemeColor(style, "--vscode-errorForeground", base.onErrorContainer),
        background = getThemeColor(style, "--vscode-sideBar-background", base.background),
        onBackground = getThemeColor(style, "--vscode-foreground", base.onBackground),
        surface = getThemeColor(style, "--vscode-editorWidget-background", base.surface),
        onSurface = getThemeColor(style, "--vscode-editorWidget-foreground", base.onSurface),
        surfaceVariant = getThemeColor(style, "--vscode-input-background", base.surfaceVariant),
        onSurfaceVariant = getThemeColor(style, "--vscode-descriptionForeground", base.onSurfaceVariant),
        outline = getThemeColor(style, "--vscode-editorWidget-border", base.outline),
        outlineVariant = getThemeColor(style, "--vscode-panel-border", base.outlineVariant),
        scrim = getThemeColor(style, "--vscode-widget-shadow", base.scrim),
        inverseSurface = getThemeColor(style, "--vscode-foreground", base.inverseSurface),
        inverseOnSurface = getThemeColor(style, "--vscode-sideBar-background", base.inverseOnSurface),
        inversePrimary = getThemeColor(style, "--vscode-charts-blue", base.inversePrimary),
        surfaceDim = getThemeColor(style, "--vscode-sideBar-background", base.surfaceDim),
        surfaceBright = getThemeColor(style, "--vscode-badge-background", base.surfaceBright),
        surfaceContainerLowest = getThemeColor(style, "--vscode-sideBar-background", base.surfaceContainerLowest),
        surfaceContainerLow = getThemeColor(style, "--vscode-editorWidget-background", base.surfaceContainerLow),
        surfaceContainer = getThemeColor(style, "--vscode-tab-inactiveBackground", base.surfaceContainer),
        surfaceContainerHigh = getThemeColor(style, "--vscode-menu-background", base.surfaceContainerHigh),
        surfaceContainerHighest = getThemeColor(style, "--vscode-input-background", base.surfaceContainerHighest),
    )
}

/**
 * Parses a CSS color string in hex (#rgb, #rrggbb, #rrggbbaa) or rgb()/rgba() notation.
 * Returns null if the string can't be parsed, so the caller decides the fallback.
 */
private fun parseCssColor(raw: String): Color? {
    val value = raw.trim()

    if (value.startsWith("#")) {
        val hex = value.removePrefix("#")
        return when (hex.length) {
            3 -> hex.map { "$it$it" }.joinToString("").let { Color(("FF$it").toLong(16)) }
            6 -> Color(("FF$hex").toLong(16))
            8 -> Color(hex.toLong(16))
            else -> null
        }
    }

    val rgbMatch = Regex("""rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*([\d.]+)\s*)?\)""").find(value)
    if (rgbMatch != null) {
        val (r, g, b) = rgbMatch.destructured
        val alpha = rgbMatch.groupValues.getOrNull(4)?.toFloatOrNull() ?: 1f
        return Color(red = r.toInt(), green = g.toInt(), blue = b.toInt(), alpha = (alpha * 255).toInt())
    }

    return null
}

/**
 * Resolves a VS Code CSS custom property to a Color, falling back to our own
 * DarkColorScheme value when the token is missing, blank, or unparsable.
 */
private fun getThemeColor(style: CSSStyleDeclaration, key: String, fallback: Color): Color {
    val rawColor = style.getPropertyValue(key).trim()
    if (rawColor.isBlank()) return fallback
    return parseCssColor(rawColor) ?: fallback
}
