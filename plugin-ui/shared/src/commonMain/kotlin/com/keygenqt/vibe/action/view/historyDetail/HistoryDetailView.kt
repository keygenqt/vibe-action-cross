/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.historyDetail

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.keygenqt.vibe.action.components.Components.Button
import com.keygenqt.vibe.action.components.Components.Text
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun HistoryDetailView(
    runId: String,
    viewModel: HistoryDetailViewModel = koinInject<HistoryDetailViewModel> { parametersOf(runId) },
    onBack: () -> Unit = {},
) {
    Column {
        Text("HistoryDetail: $runId — @todo")
        Button(onClick = onBack) { Text("Back") }
    }
}
