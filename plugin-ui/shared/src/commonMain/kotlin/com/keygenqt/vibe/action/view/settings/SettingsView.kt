/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.Components
import com.keygenqt.vibe.action.components.ScreenScaffold
import com.keygenqt.vibe.action.resources.PlatformString
import com.keygenqt.vibe.action.view.settings.components.SettingsStatusRow
import org.koin.compose.koinInject

/**
 * Settings screen — CLI status snapshot plus two housekeeping actions.
 * Deliberately not a full config editor: real editing stays in the YAML file.
 */
@Composable
fun SettingsView(
    viewModel: SettingsViewModel = koinInject<SettingsViewModel>(),
    onBack: () -> Unit = {},
) {
    val env = ViewEnvironment.current
    val status by viewModel.status.collectAsState()

    ScreenScaffold(
        title = env.bridge.res.string(PlatformString.SettingsTitle),
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = env.bridge.res.string(PlatformString.SettingsConfigDescription),
                    fontSize = 13.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Components.Button(
                    onClick = viewModel::openConfigFile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = env.bridge.res.string(PlatformString.SettingsOpenConfigButton),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                Text(
                    text = env.bridge.res.string(PlatformString.SettingsCacheDescription),
                    fontSize = 13.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))

                Components.Button(
                    onClick = viewModel::cleanCache,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = env.bridge.res.string(PlatformString.SettingsCleanCacheButton),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

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
                SettingsStatusRow(
                    label = env.bridge.res.string(PlatformString.SettingsStatusActionsLabel),
                    value = status.actionsCount.toString()
                )
                SettingsStatusRow(
                    label = env.bridge.res.string(PlatformString.SettingsStatusVersionLabel),
                    value = status.cliVersion
                )
                SettingsStatusRow(
                    label = env.bridge.res.string(PlatformString.SettingsStatusConfigLabel),
                    value = status.configVersion
                )
            }
        }
    }
}
