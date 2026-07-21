/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.base.BaseViewModel
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.models.ActionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(
    env: Environment,
    view: PlatformView,
    private val logger: Logger,
) : BaseViewModel(env, view) {

    private val _actions = MutableStateFlow(fakeActions())
    val actions: StateFlow<List<ActionModel>> = _actions.asStateFlow()

    private val _expandedActionId = MutableStateFlow<String?>(null)
    val expandedActionId: StateFlow<String?> = _expandedActionId.asStateFlow()

    init {
        logger.d { "platform: ${env.platform.name}" }
        logger.d { "view: ${view.name}" }
        logger.d { "bridge: ${bridge::class.simpleName}" }
    }

    fun toggleExpanded(id: String) {
        _expandedActionId.update { current -> if (current == id) null else id }
    }

    /**
     * @todo wire up to the real vibe-action process bridge — logging only for now.
     */
    fun runAction(id: String) {
        logger.d { "runAction: $id" }
    }

    /**
     * @todo wire up to real YAML file deletion — logging only for now.
     */
    fun deleteAction(id: String) {
        logger.d { "deleteAction: $id" }
    }

    private fun fakeActions() = listOf(
        ActionModel(
            id = "comment",
            name = "Comment",
            description = "Add explanatory comments to selection",
            isCustom = false,
        ),
        ActionModel(
            id = "explain",
            name = "Explain",
            description = "Explain what the selected code does",
            isCustom = false,
        ),
        ActionModel(
            id = "review",
            name = "Review",
            description = "Flag issues in the selection",
            isCustom = false,
        ),
        ActionModel(
            id = "rewrite-tone",
            name = "Rewrite tone",
            description = "rewrite-tone.yaml",
            isCustom = true,
            yamlPath = "rewrite-tone.yaml",
        ),
    )
}
