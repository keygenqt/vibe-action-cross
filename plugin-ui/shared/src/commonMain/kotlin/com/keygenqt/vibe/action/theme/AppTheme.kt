/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Builds the platform's native Material 3 ColorScheme — sourced from the
 * real host theme where possible, falling back to our own Material3
 * defaults (LightColorScheme/DarkColorScheme) everywhere else.
 */
@Composable
expect fun platformColorScheme(): ColorScheme

/**
 * Custom application colors used outside the standard Material theme.
 */
object ColorsApp {
    val accent: Color = Color(0xFF14B8A6)
    val starActive: Color = Color(0xFFFFB300)
}

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007ACC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0062A3),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFE8E8E8),
    onSecondary = Color(0xFF616161),
    secondaryContainer = Color(0xFFE4E6F1),
    onSecondaryContainer = Color(0xFF616161),
    tertiary = Color(0xFF652D90),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3F3F3),
    onTertiaryContainer = Color(0xFF616161),
    error = Color(0xFFE51400),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF2DEDE),
    onErrorContainer = Color(0xFFA1260D),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFF3F3F3),
    onSurface = Color(0xFF616161),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF717171),
    outline = Color(0xFFD6D6D6),
    outlineVariant = Color(0xFFD3D3D3),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF000000),
    inverseOnSurface = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFF0063D3),
    surfaceDim = Color(0xFFC4C4C4),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F3F3),
    surfaceContainer = Color(0xFFECECEC),
    surfaceContainerHigh = Color(0xFFDDDDDD),
    surfaceContainerHighest = Color(0xFFCECECE),
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0E639C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1177BB),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF2A2D2E),
    onSecondary = Color(0xFFCCCCCC),
    secondaryContainer = Color(0xFF37373D),
    onSecondaryContainer = Color(0xFFCCCCCC),
    tertiary = Color(0xFFB180D7),
    onTertiary = Color(0xFF1E1E1E),
    tertiaryContainer = Color(0xFF252526),
    onTertiaryContainer = Color(0xFFCCCCCC),
    error = Color(0xFFF14C4C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF5A1D1D),
    onErrorContainer = Color(0xFFF48771),
    background = Color(0xFF1E1E1E),
    onBackground = Color(0xFFDADADA),
    surface = Color(0xFF252526),
    onSurface = Color(0xFFCCCCCC),
    surfaceVariant = Color(0xFF3C3C3C),
    onSurfaceVariant = Color(0xFF989898),
    outline = Color(0xFF464647),
    outlineVariant = Color(0xFF404040),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFDADADA),
    inverseOnSurface = Color(0xFF1E1E1E),
    inversePrimary = Color(0xFF59A4F9),
    surfaceDim = Color(0xFF1E1E1E),
    surfaceBright = Color(0xFF4D4D4D),
    surfaceContainerLowest = Color(0xFF1E1E1E),
    surfaceContainerLow = Color(0xFF252526),
    surfaceContainer = Color(0xFF2D2D2D),
    surfaceContainerHigh = Color(0xFF303031),
    surfaceContainerHighest = Color(0xFF3C3C3C),
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = platformColorScheme(),
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
