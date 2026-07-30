/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific component declarations — resolved via expect/actual per target.
 */
expect object PlatformComponents {
    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        content: @Composable () -> Unit,
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
        enabled: Boolean,
        content: @Composable () -> Unit,
    ): (@Composable () -> Unit)? = null
}
