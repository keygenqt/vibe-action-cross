/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.jewel.foundation.theme.JewelTheme
import javax.swing.UIManager

@Composable
actual fun platformColorScheme(): ColorScheme {
    val base = if (JewelTheme.isDark) DarkColorScheme else LightColorScheme

    fun color(key: String, fallback: Color): Color = UIManager.getColor(key)?.let { Color.fromAwt(it) } ?: fallback

    return base.copy(
        primary = color("Button.default.startBackground", base.primary),
        onPrimary = color("Button.default.foreground", base.onPrimary),
        primaryContainer = color("ColorPalette.selection-bg-active", base.primaryContainer),
        onPrimaryContainer = color("Tree.selectionForeground", base.onPrimaryContainer),
        secondary = color("Button.background", base.secondary),
        onSecondary = color("Button.foreground", base.onSecondary),
        secondaryContainer = color("ColorPalette.layer-1-bg", base.secondaryContainer),
        onSecondaryContainer = color("ColorPalette.text-default", base.onSecondaryContainer),
        tertiary = color("ColorPalette.Purple8", base.tertiary),
        onTertiary = color("ColorPalette.text-over-accent", base.onTertiary),
        tertiaryContainer = color("ColorPalette.layer-2-bg", base.tertiaryContainer),
        onTertiaryContainer = color("ColorPalette.text-default", base.onTertiaryContainer),
        error = color("ColorPalette.accent-error-border", base.error),
        onError = color("ColorPalette.text-over-accent", base.onError),
        errorContainer = color("ColorPalette.Red2", base.errorContainer),
        onErrorContainer = color("ColorPalette.red-100", base.onErrorContainer),
        background = color("Panel.background", base.background),
        onBackground = color("Editor.foreground", base.onBackground),
        surface = color("ColorPalette.layer-1-bg", base.surface),
        onSurface = color("ColorPalette.text-default", base.onSurface),
        surfaceVariant = color("ColorPalette.layer-2-bg", base.surfaceVariant),
        onSurfaceVariant = color("ColorPalette.text-secondary", base.onSurfaceVariant),
        outline = color("ColorPalette.layer-1-border", base.outline),
        outlineVariant = color("ColorPalette.layer-0-border", base.outlineVariant),
        scrim = color("ColorPalette.transparent-black-50", base.scrim),
        inverseSurface = color("ColorPalette.text-default", base.inverseSurface),
        inverseOnSurface = color("ColorPalette.layer-0-bg", base.inverseOnSurface),
        inversePrimary = color("ColorPalette.Blue9", base.inversePrimary),
        surfaceDim = color("ColorPalette.layer-0-bg", base.surfaceDim),
        surfaceBright = color("ColorPalette.layer-2-bg", base.surfaceBright),
        surfaceContainerLowest = color("ColorPalette.layer-0-bg-inline", base.surfaceContainerLowest),
        surfaceContainerLow = color("ColorPalette.layer-0-bg-inline", base.surfaceContainerLow),
        surfaceContainer = color("ColorPalette.layer-1-bg", base.surfaceContainer),
        surfaceContainerHigh = color("Popup.background", base.surfaceContainerHigh),
        surfaceContainerHighest = color("ColorPalette.layer-2-bg", base.surfaceContainerHighest),
    )
}

/**
 * Converts a java.awt.Color (including JBColor/UIManager results) into a Compose Color.
 */
private fun Color.Companion.fromAwt(awt: java.awt.Color): Color = Color(awt.red, awt.green, awt.blue, awt.alpha)
