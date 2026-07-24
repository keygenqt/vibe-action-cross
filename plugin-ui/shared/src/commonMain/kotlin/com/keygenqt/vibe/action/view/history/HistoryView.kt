/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.ScreenScaffold
import com.keygenqt.vibe.action.resources.PlatformString
import com.keygenqt.vibe.action.view.history.components.HistoryList
import org.koin.compose.koinInject

/**
 * History screen — flat, read-only list of past flow runs.
 * Detail view is deferred until real run data/log parsing exists.
 */
@Composable
fun HistoryView(
    viewModel: HistoryViewModel = koinInject<HistoryViewModel>(),
    onBack: () -> Unit = {},
    onOpenDetail: (runId: String) -> Unit = {},
) {
    val env = ViewEnvironment.current
    val items by viewModel.items.collectAsState()
    val expandedItemId by viewModel.expandedItemId.collectAsState()

    ScreenScaffold(
        title = env.bridge.res.string(PlatformString.HistoryTitle),
        onBack = onBack,
        scrollable = false,
    ) {
        HistoryList(
            items = items,
            expandedItemId = expandedItemId,
            onToggleExpanded = viewModel::toggleExpanded,
            onOpenDetail = onOpenDetail,
            onDelete = viewModel::deleteItem,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
