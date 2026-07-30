/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.LoadingLottieAnimation
import com.keygenqt.vibe.action.components.NotificationHandler
import com.keygenqt.vibe.action.components.ScreenScaffold
import com.keygenqt.vibe.action.resources.PlatformString
import com.keygenqt.vibe.action.view.settings.components.CacheSection
import com.keygenqt.vibe.action.view.settings.components.ConfigSection
import com.keygenqt.vibe.action.view.settings.components.StatusSection
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
    val isLoading by viewModel.isLoading.collectAsState()
    val notification by viewModel.notification.collectAsState()

    NotificationHandler(
        notification = notification,
        onClear = viewModel::clearNotification,
    )

    ScreenScaffold(
        title = env.bridge.res.string(PlatformString.SettingsTitle),
        onBack = onBack,
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingLottieAnimation(modifier = Modifier.fillMaxSize())
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                ConfigSection(
                    onOpenConfig = viewModel::openConfigFile,
                    isConfigAvailable = status != null,
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                CacheSection(onCleanCache = viewModel::cleanCache)

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                StatusSection(status = status)
            }
        }
    }
}
