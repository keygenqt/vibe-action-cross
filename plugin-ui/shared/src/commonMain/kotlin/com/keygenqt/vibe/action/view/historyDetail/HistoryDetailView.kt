/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.historyDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.components.ScreenScaffold
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun HistoryDetailView(
    runId: String,
    viewModel: HistoryDetailViewModel = koinInject<HistoryDetailViewModel> { parametersOf(runId) },
    onBack: () -> Unit = {},
) {
    ScreenScaffold(
        title = runId,
        onBack = onBack,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "History detail: $runId — @todo")
        }
    }
}
