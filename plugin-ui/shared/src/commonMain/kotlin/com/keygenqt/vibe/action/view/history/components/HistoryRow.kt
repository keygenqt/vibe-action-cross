/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.models.HistoryItemModel

/**
 * A single row: status icon, name/timestamp, circular button that navigates
 * to detail. Clicking the row body (not the button) toggles an inline Delete option.
 */
@Composable
fun HistoryRow(
    item: HistoryItemModel,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onOpenDetail: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.width(34.dp)) {
                        Icon(
                            imageVector = if (item.isSuccess) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (item.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(text = item.actionName, fontSize = 14.sp)
                }
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 34.dp),
                    text = item.timestamp,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HistoryDetailButton(onClick = onOpenDetail)
        }

        if (expanded) {
            HistoryRowExpandedMenu(onDelete = onDelete)
        }

        HorizontalDivider()
    }
}

/**
 * Circular button that opens the run's detail screen. Its own clickable
 * consumes the tap, so it doesn't also toggle the row's expand/collapse.
 */
@Composable
private fun HistoryDetailButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open detail",
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
    }
}

/**
 * Delete option revealed when a row is expanded, indented to align
 * under the row's name/timestamp text.
 */
@Composable
private fun HistoryRowExpandedMenu(onDelete: () -> Unit) {
    Column {
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDelete)
                .padding(start = 50.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp),
            )
            Text(text = "Delete", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}
