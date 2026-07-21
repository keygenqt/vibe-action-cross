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
import androidx.compose.material3.Button as MButton
import androidx.compose.material3.Text as MText

/**
 * Unified component factory — delegates to Jewel (IntelliJ) or Material3 via expect/actual.
 */
object Components {
    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        PlatformComponents.Button(onClick, modifier, content)?.invoke() ?: run {
            MButton(onClick = onClick, modifier = modifier) {
                content()
            }
        }
    }
}
