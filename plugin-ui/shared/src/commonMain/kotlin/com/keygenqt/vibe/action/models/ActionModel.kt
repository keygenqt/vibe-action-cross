/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.models

import com.keygenqt.vibe.action.command.ActionApi
import com.keygenqt.vibe.action.command.ActionArg

/**
 * A single runnable flow shown in the Actions list — either a built-in
 * flow bundled with vibe-action, or a custom YAML flow discovered in the project.
 */
data class ActionModel(
    val id: String,
    val command: List<String>,
    val name: String,
    val description: String,
    val isCustom: Boolean,
    val group: String? = null,
    val groupAbout: String? = null,
    val yamlPath: String? = null,
    val args: List<ActionArg> = emptyList(),
    val api: ActionApi,
    val isStarred: Boolean = false,
)
