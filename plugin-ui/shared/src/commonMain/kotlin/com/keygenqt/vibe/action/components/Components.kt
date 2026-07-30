/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button as MButton

/**
 * Unified component factory — delegates to Jewel (IntelliJ) or Material3 via expect/actual.
 */
object Components {
    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        content: @Composable () -> Unit,
    ) {
        PlatformComponents.Button(onClick, modifier, enabled, content)?.invoke() ?: run {
            MButton(
                onClick = onClick,
                modifier = modifier.height(28.dp),
                enabled = enabled,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            ) {
                content()
            }
        }
    }
}
