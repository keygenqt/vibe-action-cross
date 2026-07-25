/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.base.BaseViewModel
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.command.CommandOutput
import com.keygenqt.vibe.action.command.CommandProvider
import com.keygenqt.vibe.action.models.ActionModel
import com.keygenqt.vibe.action.models.builtInActionIds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

class MainViewModel(
    env: Environment,
    view: PlatformView,
    private val commandProvider: CommandProvider,
    private val logger: Logger,
) : BaseViewModel(env, view) {

    private val _actions = MutableStateFlow<List<ActionModel>>(emptyList())
    val actions: StateFlow<List<ActionModel>> = _actions.asStateFlow()

    private val _expandedActionId = MutableStateFlow<String?>(null)
    val expandedActionId: StateFlow<String?> = _expandedActionId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var actionsPath: String? = null

    // VM is a Koin singleton living for the whole app lifetime:
    // the file-exists bridge is captured once — platform bridges never swap it.
    private val fileExists: (suspend (String) -> Boolean)? = env.bridge.sys.fileExists

    // Serializes loadData() calls — init and refresh() must not overlap.
    private val loadMutex = Mutex()

    init {
        loadData()
    }

    /**
     * Reloads CLI status and action list. Exposed for retry/pull-to-refresh —
     * the VM is a singleton, so [loadData] would otherwise run only once per app lifetime.
     */
    fun refresh() = loadData()

    /**
     * Loads CLI status and action list via [CommandProvider].
     */
    private fun loadData() {
        viewModelScope.launch {
            loadMutex.withLock {
                _isLoading.value = true
                _error.value = null
                try {
                    // Collect status output from CLI and extract actions path.
                    // A failing status must not block the action list —
                    // actionsPath is only needed to resolve custom actions' yaml files.
                    val status = try {
                        commandProvider.status()
                            .filterIsInstance<CommandOutput.Status>()
                            .firstOrNull()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.w(e) { "Status command failed, continuing without actionsPath" }
                        null
                    }
                    actionsPath = status?.actionsPath

                    // Collect action list from CLI and map to ActionModel instances.
                    val actionOutputs = commandProvider.actions()
                    val models = actionOutputs.filterIsInstance<CommandOutput.Actions>().map { out ->
                        val isCustom = out.name !in builtInActionIds
                        ActionModel(
                            id = out.name,
                            name = out.name.replaceFirstChar { it.uppercase() },
                            description = out.about,
                            isCustom = isCustom,
                            yamlPath = null, // resolved below for custom actions only
                        )
                    }

                    // Resolve yaml files for custom actions (.yaml, then .yml).
                    // Checks are fanned out in parallel: on VS Code each call is a
                    // postMessage round-trip to the extension host.
                    val dir = actionsPath
                    _actions.value = if (fileExists != null && dir != null) {
                        coroutineScope {
                            models.map { action ->
                                async {
                                    val yaml = "$dir/${action.id}.yaml"
                                    val yml = "$dir/${action.id}.yml"
                                    val path = when {
                                        fileExists(yaml) -> yaml
                                        fileExists(yml) -> yml
                                        else -> null
                                    }
                                    action.copy(yamlPath = path)
                                }
                            }.awaitAll()
                        }
                    } else {
                        models
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _error.value = e.message ?: "Unknown error"
                    logger.e(e) { "Load error" }
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun toggleExpanded(id: String) {
        _expandedActionId.update { current -> if (current == id) null else id }
    }

    fun runAction(id: String) {
        val action = _actions.value.find { it.id == id }
        val pathInfo = action?.yamlPath?.let { ", yamlPath=$it" } ?: ""
        logger.d { "Запуск экшена: $id$pathInfo" }
        // TODO: делегировать запуск в CommandProvider, когда появится runAction()
    }

    fun deleteAction(id: String) {
        val action = _actions.value.find { it.id == id } ?: return
        if (!action.isCustom) {
            logger.w { "Нельзя удалить встроенный экшен: $id" }
            return
        }
        logger.d { "Удаление экшена: $id" }
        // TODO: делегировать удаление в CommandProvider
    }
}
