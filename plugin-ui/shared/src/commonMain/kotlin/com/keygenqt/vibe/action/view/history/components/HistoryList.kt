/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.history.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.keygenqt.vibe.action.models.HistoryItemModel

/**
 * Full flat list of history rows — no grouping, no accordion, just runs in order.
 */
@Composable
fun HistoryList(
    items: List<HistoryItemModel>,
    expandedItemId: String?,
    onToggleExpanded: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(items, key = { it.id }) { item ->
            HistoryRow(
                item = item,
                expanded = item.id == expandedItemId,
                onToggleExpanded = { onToggleExpanded(item.id) },
                onOpenDetail = { onOpenDetail(item.id) },
                onDelete = { onDelete(item.id) },
            )
        }
    }
}
