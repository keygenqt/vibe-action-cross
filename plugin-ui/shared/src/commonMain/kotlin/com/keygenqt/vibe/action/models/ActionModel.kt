/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.keygenqt.vibe.action.command.ActionApi
import com.keygenqt.vibe.action.command.ActionArg

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
    val args: List<ActionArg> = emptyList(),
    val api: ActionApi,
)

/**
 * Built-in action ids from vibe-action CLI.
 * Used to distinguish built-in from custom actions.
 */
val builtInActionIds = setOf(
    "comment",
    "commit",
    "describe",
    "explain",
    "extract",
    "faq",
    "fetch",
    "find",
    "mock",
    "naming",
    "regex",
    "review",
    "scan",
    "spellcheck",
    "synonyms",
    "sysinfo",
    "tone",
    "translate-deep",
    "translate-fast",
    "whois",
)

/**
 * Resolves a display icon for this action from its id. Built-in flows get
 * a specific icon; anything unrecognized (all custom YAML flows) falls
 * back to a generic flow icon.
 */
val ActionModel.icon: ImageVector
    get() = when (id) {
        "comment" -> Icons.AutoMirrored.Filled.Message
        "commit" -> Icons.Default.Done
        "describe" -> Icons.Default.Info
        "explain" -> Icons.Default.Lightbulb
        "extract" -> Icons.Default.Search
        "faq" -> Icons.Default.QuestionAnswer
        "fetch" -> Icons.Default.Download
        "find" -> Icons.Default.Search
        "mock" -> Icons.Default.Add
        "naming" -> Icons.Default.Edit
        "regex" -> Icons.Default.Code
        "review" -> Icons.AutoMirrored.Filled.ListAlt
        "scan" -> Icons.Default.Search
        "spellcheck" -> Icons.Default.Check
        "synonyms" -> Icons.Default.Edit
        "sysinfo" -> Icons.Default.Info
        "tone" -> Icons.Default.Edit
        "translate-deep", "translate-fast" -> Icons.Default.Star
        "whois" -> Icons.Default.Person
        else -> Icons.Default.Bolt
    }
