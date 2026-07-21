/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.DefaultButton as JButton

/**
 * JVM target — provides Jewel components for IntelliJ plugin, falls back to Material3 for Desktop.
 */
actual object PlatformComponents {
    @Composable
    actual fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        content: @Composable () -> Unit,
    ): (@Composable () -> Unit)? = {
        JButton(onClick = onClick, modifier = modifier) {
            content()
        }
    }
}
