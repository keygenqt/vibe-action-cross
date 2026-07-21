/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.ScreenScaffold
import com.keygenqt.vibe.action.resources.PlatformString
import org.koin.compose.koinInject

@Composable
fun SettingsView(
    viewModel: SettingsViewModel = koinInject<SettingsViewModel>(),
    onBack: () -> Unit = {},
) {
    val env = ViewEnvironment.current

    ScreenScaffold(
        title = env.bridge.res.string(PlatformString.SettingsTitle),
        onBack = onBack,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Settings — @todo")
        }
    }
}
