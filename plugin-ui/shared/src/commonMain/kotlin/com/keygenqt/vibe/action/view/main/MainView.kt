/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.ScreenScaffold
import com.keygenqt.vibe.action.resources.PlatformIcon
import org.koin.compose.koinInject

@Composable
fun MainView(
    viewModel: MainViewModel = koinInject<MainViewModel>(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
) {
    val env = ViewEnvironment.current

    ScreenScaffold(
        title = "Actions",
        titleIcon = {
            Icon(
                painter = env.bridge.res.icon(PlatformIcon.AppIcon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        },
        actions = {
            IconButton(
                onClick = onNavigateToHistory,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    modifier = Modifier.size(16.dp),
                )
            }
            IconButton(
                onClick = onNavigateToAbout,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About",
                    modifier = Modifier.size(16.dp),
                )
            }
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(16.dp),
                )
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Main View")
        }
    }
}
