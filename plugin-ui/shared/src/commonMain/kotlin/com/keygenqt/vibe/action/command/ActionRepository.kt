/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.command

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.models.ActionModel
import com.keygenqt.vibe.action.models.builtInActionIds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository for action-related business logic.
 * Maps CLI outputs to domain models and resolves local file paths.
 */
class ActionRepository(
    private val env: Environment,
    private val commandProvider: CommandProvider,
    private val toolingRepository: ToolingRepository,
    private val logger: Logger,
) {
    /**
     * Loads the full action list with resolved YAML paths for custom actions.
     */
    suspend fun loadActions(): List<ActionModel> {
        val dir = toolingRepository.getStatus(forceRefresh = true)?.actionsPath
        val fileExists = env.bridge.sys.fileExists

        val actionOutputs = commandProvider.actions()
        val models = actionOutputs.filterIsInstance<CommandOutput.Actions>()
            .filter { it.api != null }
            .map { out ->
                val isCustom = out.name !in builtInActionIds
                ActionModel(
                    id = out.name,
                    name = out.name.replaceFirstChar { it.uppercase() },
                    description = out.about,
                    isCustom = isCustom,
                    args = out.args,
                    api = out.api!!,
                    yamlPath = null,
                )
            }

        return if (fileExists != null && dir != null) {
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
    }

    /**
     * Executes an action with arguments resolved from IDE sources (selection, clipboard).
     */
    suspend fun executeAction(
        action: ActionModel,
        onSuccess: () -> Unit,
        onEmpty: () -> Unit,
    ) {
        val args = mutableListOf(action.id)

        for (arg in action.args) {
            val source = action.api.args[arg.name] ?: continue
            val value: String? = when (source) {
                ActionApiSource.Selection -> getSuspendValue(env.bridge.sys.getSelectedText)
                ActionApiSource.Clipboard -> getSuspendValue(env.bridge.sys.getClipboardText)
            }

            if (value.isNullOrEmpty()) continue

            val flag = if (arg.short != null) "-${arg.short}" else "--${arg.name}"

            when (arg.input) {
                "bool" -> {
                    val isTrue = value.lowercase() in listOf("true", "yes", "да", "1")
                    if (isTrue) args.add(flag)
                }

                else -> {
                    args.add(flag)
                    args.add(value)
                }
            }
        }

        val newCode = commandProvider.execute(args)
            .filterIsInstance<CommandOutput.Success>()
            .firstOrNull()?.message

        if (newCode.isNullOrEmpty()) {
            onEmpty()
        } else {
            when (action.api.output) {
                ActionApiTarget.Clipboard -> env.bridge.sys.setClipboardText?.invoke(newCode)
                ActionApiTarget.Replace -> env.bridge.sys.replaceSelectedText?.invoke(newCode)
                ActionApiTarget.Dialog -> env.bridge.sys.showTextDialog?.invoke(action.name, newCode)
            }
            onSuccess()
        }
    }

    private suspend fun getSuspendValue(callback: (((String?) -> Unit) -> Unit)?): String? {
        if (callback == null) return null
        return suspendCancellableCoroutine { cont ->
            try {
                callback { text ->
                    if (cont.isActive) cont.resume(text)
                }
            } catch (e: Exception) {
                if (cont.isActive) cont.resumeWithException(e)
            }
        }
    }
}
