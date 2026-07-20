/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.components.Components.Text
import com.keygenqt.vibe.action.components.ScreenScaffold
import org.koin.compose.koinInject

@Composable
fun MainView(
    viewModel: MainViewModel = koinInject<MainViewModel>(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
) {
    ScreenScaffold(
        title = "Actions",
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
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Page")
        }
    }
}
