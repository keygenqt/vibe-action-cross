/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.models.ActionModel
import com.keygenqt.vibe.action.resources.PlatformString
import com.keygenqt.vibe.action.theme.ColorsApp

/**
 * A collapsible section of the actions list. [key] doubles as the
 * persistence id; [iconTint] null falls back to the header text color.
 */
private data class ActionSection(
    val key: String,
    val title: String,
    val about: String?,
    val icon: ImageVector,
    val items: List<ActionModel>,
    val iconTint: Color? = null,
)

/**
 * Full list of action sections, each collapsible via its header.
 */
@Composable
fun ActionsList(
    actions: List<ActionModel>,
    expandedActionId: String?,
    runningActionId: String?,
    showDescriptions: Boolean,
    showVersionBanner: Boolean,
    expandedGroups: Set<String>,
    onToggleGroup: (String) -> Unit,
    onToggleExpanded: (String) -> Unit,
    onRun: (String) -> Unit,
    onEdit: (String) -> Unit,
    onCancel: () -> Unit,
    onDelete: ((String) -> Unit)?,
    onToggleStar: (String) -> Unit,
) {
    val env = ViewEnvironment.current
    val favorites = actions.filter { it.isStarred }
    val custom = actions.filter { !it.isStarred && it.isCustom && it.group == null }
    val grouped = actions.filter { !it.isStarred && it.group != null }
    val default = actions.filter { !it.isStarred && !it.isCustom && it.group == null }
    // Group sections in first-appearance order (CLI order).
    val groupOrder = grouped.mapNotNull { it.group }.distinct()

    // Resolve strings outside buildList — res.string is @Composable
    val titleFavorite = env.bridge.res.string(PlatformString.ActionGroupFavorite)
    val aboutFavorite = env.bridge.res.string(PlatformString.ActionGroupFavoriteAbout)
    val titleCustom = env.bridge.res.string(PlatformString.ActionGroupCustom)
    val aboutCustom = env.bridge.res.string(PlatformString.ActionGroupCustomAbout)
    val titleDefault = env.bridge.res.string(PlatformString.ActionGroupDefault)
    val aboutDefault = env.bridge.res.string(PlatformString.ActionGroupDefaultAbout)

    val sections = buildList {
        if (favorites.isNotEmpty()) {
            add(
                ActionSection(
                    key = ActionSectionKey.Favorite.name,
                    title = titleFavorite,
                    about = aboutFavorite,
                    icon = Icons.Default.FolderSpecial,
                    items = favorites,
                    iconTint = ColorsApp.starActive,
                ),
            )
        }
        if (custom.isNotEmpty()) {
            add(
                ActionSection(
                    key = ActionSectionKey.Custom.name,
                    title = titleCustom,
                    about = aboutCustom,
                    icon = Icons.Default.FolderShared,
                    items = custom,
                    iconTint = ColorsApp.accent,
                ),
            )
        }
        for (group in groupOrder) {
            add(
                ActionSection(
                    key = group,
                    title = group.replaceFirstChar { it.uppercase() },
                    about = grouped.firstOrNull { it.group == group }?.groupAbout,
                    icon = Icons.Default.Folder,
                    items = grouped.filter { it.group == group },
                    iconTint = MaterialTheme.colorScheme.primary,
                ),
            )
        }
        if (default.isNotEmpty()) {
            add(
                ActionSection(
                    key = ActionSectionKey.Default.name,
                    title = titleDefault,
                    about = aboutDefault,
                    icon = Icons.Default.Folder,
                    items = default,
                    iconTint = ColorsApp.accent,
                ),
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        sections.forEachIndexed { index, section ->
            if (showVersionBanner || index > 0) {
                HorizontalDivider()
            }
            ActionGroupHeader(
                title = section.title,
                about = section.about.takeIf { showDescriptions },
                icon = section.icon,
                iconTint = section.iconTint,
                expanded = section.key in expandedGroups,
                onToggle = { onToggleGroup(section.key) },
            )
            if (index == sections.lastIndex && section.key !in expandedGroups) {
                HorizontalDivider()
            }
            AnimatedVisibility(
                visible = section.key in expandedGroups,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider()
                    section.items.forEachIndexed { rowIdx, item ->
                        RenderActionRow(
                            action = item,
                            expandedActionId = expandedActionId,
                            runningActionId = runningActionId,
                            showDescriptions = showDescriptions,
                            showTopDivider = rowIdx > 0,
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
    }
}

/**
 * Collapsible section header: icon, bold title, and a chevron that
 * rotates on expand/collapse.
 */
@Composable
private fun ActionGroupHeader(
    title: String,
    about: String?,
    icon: ImageVector,
    iconTint: Color?,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val chevronRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column {
            Spacer(Modifier.height(4.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (about != null) {
                Text(
                    text = about,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
            }
        }
        Column {
            Spacer(Modifier.height(4.dp))
            Row {
                Icon(
                    imageVector = Icons.Default.ExpandCircleDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(chevronRotation),
                )
                Spacer(Modifier.width(2.dp))
            }
        }
    }
}

@Composable
private fun RenderActionRow(
    action: ActionModel,
    expandedActionId: String?,
    runningActionId: String?,
    showDescriptions: Boolean,
    showTopDivider: Boolean,
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
        showTopDivider = showTopDivider,
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
