/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.resources.PlatformString

@Composable
fun AppearanceSection(
    showDescriptions: Boolean,
    onToggleShowDescriptions: () -> Unit,
) {
    val env = ViewEnvironment.current

    Column {
        Text(
            text = env.bridge.res.string(PlatformString.SettingsAppearanceTitle),
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = env.bridge.res.string(PlatformString.SettingsAppearanceDescription),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = env.bridge.res.string(PlatformString.SettingsShowDescriptionsLabel),
                fontSize = 13.sp,
            )
            Switch(
                checked = showDescriptions,
                onCheckedChange = { onToggleShowDescriptions() },
                modifier = Modifier.scale(0.8f),
                colors = SwitchDefaults.colors(
                    // Enabled & Checked
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedBorderColor = MaterialTheme.colorScheme.primary,
                    checkedIconColor = MaterialTheme.colorScheme.onPrimary,
                    // Enabled & Unchecked
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                    uncheckedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
