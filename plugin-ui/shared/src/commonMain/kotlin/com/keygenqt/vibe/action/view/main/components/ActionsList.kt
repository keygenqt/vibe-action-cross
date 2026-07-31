/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.models.ActionModel
import com.keygenqt.vibe.action.resources.PlatformString

/**
 * Full list of action rows.
 */
@Composable
fun ActionsList(
    actions: List<ActionModel>,
    expandedActionId: String?,
    runningActionId: String?,
    showDescriptions: Boolean,
    onToggleExpanded: (String) -> Unit,
    onRun: (String) -> Unit,
    onEdit: (String) -> Unit,
    onCancel: () -> Unit,
    onDelete: ((String) -> Unit)?,
    onToggleStar: (String) -> Unit,
) {
    val env = ViewEnvironment.current
    val favorites = actions.filter { it.isStarred }
    val custom = actions.filter { !it.isStarred && it.isCustom }
    val default = actions.filter { !it.isStarred && !it.isCustom }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (favorites.isNotEmpty()) {
            ActionGroupHeader(env.bridge.res.string(PlatformString.ActionGroupFavorite))
            favorites.forEach {
                RenderActionRow(
                    action = it,
                    expandedActionId = expandedActionId,
                    runningActionId = runningActionId,
                    showDescriptions = showDescriptions,
                    onToggleExpanded = onToggleExpanded,
                    onRun = onRun,
                    onEdit = onEdit,
                    onCancel = onCancel,
                    onDelete = onDelete,
                    onToggleStar = onToggleStar,
                )
            }
        }
        if (custom.isNotEmpty()) {
            ActionGroupHeader(env.bridge.res.string(PlatformString.ActionGroupCustom))
            custom.forEach {
                RenderActionRow(
                    action = it,
                    expandedActionId = expandedActionId,
                    runningActionId = runningActionId,
                    showDescriptions = showDescriptions,
                    onToggleExpanded = onToggleExpanded,
                    onRun = onRun,
                    onEdit = onEdit,
                    onCancel = onCancel,
                    onDelete = onDelete,
                    onToggleStar = onToggleStar,
                )
            }
        }
        if (default.isNotEmpty()) {
            if (favorites.isNotEmpty() || custom.isNotEmpty()) {
                ActionGroupHeader(env.bridge.res.string(PlatformString.ActionGroupDefault))
            }
            default.forEach {
                RenderActionRow(
                    action = it,
                    expandedActionId = expandedActionId,
                    runningActionId = runningActionId,
                    showDescriptions = showDescriptions,
                    onToggleExpanded = onToggleExpanded,
                    onRun = onRun,
                    onEdit = onEdit,
                    onCancel = onCancel,
                    onDelete = onDelete,
                    onToggleStar = onToggleStar,
                )
            }
        }
    }
}

@Composable
private fun ActionGroupHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun RenderActionRow(
    action: ActionModel,
    expandedActionId: String?,
    runningActionId: String?,
    showDescriptions: Boolean,
    onToggleExpanded: (String) -> Unit,
    onRun: (String) -> Unit,
    onEdit: (String) -> Unit,
    onCancel: () -> Unit,
    onDelete: ((String) -> Unit)?,
    onToggleStar: (String) -> Unit,
) {
    ActionRow(
        action = action,
        expanded = action.id == expandedActionId,
        isRunning = action.id == runningActionId,
        showDescriptions = showDescriptions,
        onToggleExpanded = { onToggleExpanded(action.id) },
        onRun = { onRun(action.id) },
        onEdit = { onEdit(action.id) },
        onCancel = onCancel,
        onDelete = if (action.isCustom) {
            { onDelete?.invoke(action.id) }
        } else {
            null
        },
        onToggleStar = { onToggleStar(action.id) },
    )
}
