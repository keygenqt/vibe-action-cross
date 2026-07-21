package com.keygenqt.vibe.action.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A single runnable flow shown in the Actions list — either a built-in
 * flow bundled with vibe-action, or a custom YAML flow discovered in the project.
 */
data class ActionModel(
    val id: String,
    val name: String,
    val description: String,
    val isCustom: Boolean,
    val yamlPath: String? = null,
)

/**
 * Resolves a display icon for this action from its id. Built-in flows get
 * a specific icon; anything unrecognized (all custom YAML flows) falls
 * back to a generic flow icon.
 */
val ActionModel.icon: ImageVector
    get() = when (id) {
        "comment" -> Icons.AutoMirrored.Filled.Message
        "explain" -> Icons.Default.Lightbulb
        "review" -> Icons.AutoMirrored.Filled.ListAlt
        else -> Icons.Default.Bolt
    }
