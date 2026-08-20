/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.models.ActionModel
import com.keygenqt.vibe.action.theme.ColorsApp

/**
 * A single action row: icon, name/description, circular play/stop button.
 * Clicking the row body (not the button) toggles the inline Edit/Delete menu.
 */
@Composable
fun ActionRow(
    action: ActionModel,
    expanded: Boolean,
    isRunning: Boolean,
    showDescriptions: Boolean,
    onToggleExpanded: () -> Unit,
    onRun: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)?,
    onToggleStar: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = action.yamlPath != null,
                    onClick = onToggleExpanded,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = if (showDescriptions) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.width(34.dp)) {
                        Box(modifier = Modifier.padding(top = if (showDescriptions) 4.dp else 0.dp)) {
                            IconButton(
                                onClick = onToggleStar,
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (action.isStarred) ColorsApp.starActive else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    Text(text = action.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                if (showDescriptions) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 34.dp),
                        text = action.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(modifier = Modifier.padding(top = if (showDescriptions) 10.dp else 0.dp)) {
                ActionPlayButton(
                    isRunning = isRunning,
                    onRun = onRun,
                    onCancel = onCancel,
                )
            }
        }

        if (expanded && action.yamlPath != null) {
            ActionRowExpandedMenu(
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }

        HorizontalDivider()
    }
}

/**
 * Circular play/stop button — runs or cancels the action.
 * Changes appearance depending on [isRunning].
 */
@Composable
fun ActionPlayButton(
    isRunning: Boolean,
    onRun: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                if (isRunning) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
            )
            .clickable(onClick = if (isRunning) onCancel else onRun),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = if (isRunning) "Cancel" else "Run",
            tint = Color.White,
            modifier = Modifier.size(14.dp),
        )
    }
}

/**
 * Edit / Delete options revealed when a row is expanded, indented to align
 * under the row's name/description text.
 */
@Composable
fun ActionRowExpandedMenu(
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    Column {
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(start = 50.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(text = "Edit", fontSize = 13.sp)
        }
        if (onDelete != null) {
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
}
