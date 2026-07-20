/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

/**
 * Platform-specific component declarations — resolved via expect/actual per target.
 */
expect object PlatformComponents {
    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        content: @Composable () -> Unit,
    ): (@Composable () -> Unit)?

    @Composable
    fun Text(
        text: String,
        modifier: Modifier,
        fontSize: TextUnit,
        fontWeight: FontWeight?,
        color: Color,
        textAlign: TextAlign,
    ): (@Composable () -> Unit)?
}

/**
 * Shared no-op [PlatformComponents] implementation for targets without native component overrides.
 */
object PlatformComponentsEmpty {
    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        content: @Composable () -> Unit,
    ): (@Composable () -> Unit)? = null

    @Composable
    fun Text(
        text: String,
        modifier: Modifier,
        fontSize: TextUnit,
        fontWeight: FontWeight?,
        color: Color,
        textAlign: TextAlign,
    ): (@Composable () -> Unit)? = null
}
