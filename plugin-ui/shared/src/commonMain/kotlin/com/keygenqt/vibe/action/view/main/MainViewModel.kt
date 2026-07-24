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
import com.keygenqt.vibe.action.models.builtInActionIds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class MainViewModel(
    env: Environment,
    view: PlatformView,
    private val logger: Logger,
) : BaseViewModel(env, view) {

    private val json = Json { ignoreUnknownKeys = true }

    private val _actions = MutableStateFlow<List<ActionModel>>(emptyList())
    val actions: StateFlow<List<ActionModel>> = _actions.asStateFlow()

    private val _expandedActionId = MutableStateFlow<String?>(null)
    val expandedActionId: StateFlow<String?> = _expandedActionId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        logger.d { "platform: ${env.platform.name}" }
        logger.d { "view: ${view.name}" }
        logger.d { "bridge: ${bridge::class.simpleName}" }
        loadActions()
    }

    private fun loadActions() {
        val runCli = env.bridge.sys.runCli
        if (runCli == null) {
            logger.w { "runCli not available on this platform" }
            _error.value = "CLI not available"
            return
        }

        _isLoading.value = true
        _error.value = null

        runCli(emptyList(), { line ->
            parseActionLine(line)
        }, { exitCode ->
            _isLoading.value = false
            if (exitCode != 0) {
                _error.value = "CLI exited with code $exitCode"
            }
        })
    }

    private fun parseActionLine(line: String) {
        try {
            val jsonElement = json.parseToJsonElement(line)
            val element = jsonElement as? JsonObject ?: return

            val message = element["message"] as? JsonObject ?: return

            val export = (message["export"] as? JsonPrimitive)?.content
            if (export != "actions") return

            val name = (message["name"] as? JsonPrimitive)?.content ?: return
            val about = (message["about"] as? JsonPrimitive)?.content ?: ""

            logger.d { "Action found: name=$name" }

            val action = ActionModel(
                id = name,
                name = name.replaceFirstChar { it.uppercase() },
                description = about,
                isCustom = name !in builtInActionIds,
                yamlPath = if (name !in builtInActionIds) "$name.yaml" else null,
            )

            _actions.update { current ->
                if (current.none { it.id == action.id }) {
                    current + action
                } else {
                    current
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Parse error for line: $line" }
        }
    }

    fun toggleExpanded(id: String) {
        _expandedActionId.update { current -> if (current == id) null else id }
    }

    fun runAction(id: String) {
        val runCli = env.bridge.sys.runCli
        if (runCli == null) {
            logger.w { "runCli not available" }
            return
        }

        logger.d { "runAction: $id" }
        // @todo: implement action execution with progress UI
    }

    fun deleteAction(id: String) {
        val action = _actions.value.find { it.id == id } ?: return
        if (!action.isCustom) {
            logger.w { "Cannot delete built-in action: $id" }
            return
        }

        logger.d { "deleteAction: $id" }
        // @todo: implement file deletion + refresh
    }
}
