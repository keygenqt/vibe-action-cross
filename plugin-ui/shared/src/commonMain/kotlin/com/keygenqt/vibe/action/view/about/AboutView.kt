/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.about

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.keygenqt.vibe.action.components.Components.Button
import com.keygenqt.vibe.action.components.Components.Text
import org.koin.compose.koinInject

@Composable
fun AboutView(
    viewModel: AboutViewModel = koinInject<AboutViewModel>(),
    onBack: () -> Unit = {},
) {
    Column {
        Text("About — @todo")
        Button(onClick = onBack) { Text("Back") }
    }
}
