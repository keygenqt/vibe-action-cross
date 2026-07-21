package com.keygenqt.vibe.action.view.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.keygenqt.vibe.action.models.ActionModel

/**
 * Full list of action rows.
 */
@Composable
fun ActionsList(
    actions: List<ActionModel>,
    expandedActionId: String?,
    onToggleExpanded: (String) -> Unit,
    onRun: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        actions.forEach { action ->
            ActionRow(
                action = action,
                expanded = action.id == expandedActionId,
                onToggleExpanded = { onToggleExpanded(action.id) },
                onRun = { onRun(action.id) },
                onDelete = { onDelete(action.id) },
            )
        }
    }
}
