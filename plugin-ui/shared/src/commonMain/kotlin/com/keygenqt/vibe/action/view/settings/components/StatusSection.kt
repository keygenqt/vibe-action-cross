/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.models.SettingsStatusModel
import com.keygenqt.vibe.action.resources.PlatformString

@Composable
fun StatusSection(
    status: SettingsStatusModel?,
) {
    val env = ViewEnvironment.current

    Column {
        Text(
            text = env.bridge.res.string(PlatformString.SettingsStatusDescription),
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(16.dp),
        ) {
            if (status?.actionsCustomCount == null || status.actionsCustomCount == 0) {
                SettingsStatusRow(
                    label = env.bridge.res.string(PlatformString.SettingsStatusActionsLabel),
                    value = status?.actionsCount?.toString() ?: "-",
                )
            } else {
                SettingsStatusRow(
                    label = env.bridge.res.string(PlatformString.SettingsStatusActionsLabel),
                    value = "${status.actionsCount} (${status.actionsDefaultCount}/${status.actionsCustomCount})",
                )
            }
            SettingsStatusRow(
                label = env.bridge.res.string(PlatformString.SettingsStatusVersionLabel),
                value = status?.cliVersion ?: "-",
            )
            SettingsStatusRow(
                label = env.bridge.res.string(PlatformString.SettingsStatusConfigLabel),
                value = status?.configVersion ?: "-",
            )
        }
    }
}
