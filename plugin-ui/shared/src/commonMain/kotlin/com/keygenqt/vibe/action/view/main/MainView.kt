/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.keygenqt.vibe.action.components.Components.Button
import com.keygenqt.vibe.action.components.Components.Text
import org.koin.compose.koinInject

@Composable
fun MainView(
    viewModel: MainViewModel = koinInject<MainViewModel>(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
) {
    Column {
        Text("Main — @todo")
        Button(onClick = onNavigateToSettings) { Text("Settings") }
        Button(onClick = onNavigateToHistory) { Text("History") }
        Button(onClick = onNavigateToAbout) { Text("About") }
    }
}
