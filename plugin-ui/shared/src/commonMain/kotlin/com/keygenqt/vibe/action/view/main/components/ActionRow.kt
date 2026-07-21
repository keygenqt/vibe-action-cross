package com.keygenqt.vibe.action.view.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.models.ActionModel
import com.keygenqt.vibe.action.models.icon

/**
 * A single action row: icon, name/description, circular play button.
 * Clicking the row body (not the play button) toggles the inline Edit/Delete menu.
 */
@Composable
fun ActionRow(
    action: ActionModel,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onRun: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(modifier = Modifier.padding(top = 5.dp)) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = action.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = action.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ActionPlayButton(onClick = onRun)
        }

        if (expanded) {
            ActionRowExpandedMenu(onEdit = { /* @todo: open YAML in editor */ }, onDelete = onDelete)
        }

        HorizontalDivider()
    }
}
