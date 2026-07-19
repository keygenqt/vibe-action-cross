/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.history

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.keygenqt.vibe.action.components.Components.Button
import com.keygenqt.vibe.action.components.Components.Text
import org.koin.compose.koinInject

@Composable
fun HistoryView(
    viewModel: HistoryViewModel = koinInject<HistoryViewModel>(),
    onBack: () -> Unit = {},
    onOpenDetail: (runId: String) -> Unit = {},
) {
    Column {
        Text("History — @todo")
        Button(onClick = onBack) { Text("Back") }
    }
}
