/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.Components
import com.keygenqt.vibe.action.resources.PlatformString

@Composable
fun ConfigSection(
    onOpenConfig: () -> Unit,
    isConfigAvailable: Boolean,
) {
    val env = ViewEnvironment.current

    Column {
        Text(
            text = env.bridge.res.string(PlatformString.SettingsConfigDescription),
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Components.Button(
            onClick = onOpenConfig,
            modifier = Modifier.fillMaxWidth(),
            enabled = isConfigAvailable,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = env.bridge.res.string(PlatformString.SettingsOpenConfigButton),
                    fontSize = 13.sp,
                )
            }
        }
    }
}
