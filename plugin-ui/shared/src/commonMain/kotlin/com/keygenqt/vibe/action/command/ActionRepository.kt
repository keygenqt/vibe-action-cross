/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.command

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.models.ActionModel
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
        val fileExists = env.bridge.sys.fileExists

        val actionOutputs = commandProvider.actions()

        // Flat (ungrouped) actions plus grouped actions extracted from Groups messages.
        val entries: List<Pair<String?, CommandOutput.Actions>> =
            actionOutputs.filterIsInstance<CommandOutput.Actions>().map { null to it } +
                    actionOutputs.filterIsInstance<CommandOutput.Groups>()
                        .flatMap { group -> group.actions.map { group.name to it } }

        val models = entries
            .filter { it.second.api != null }
            .map { (group, out) ->
                ActionModel(
                    id = if (group != null) "$group/${out.name}" else out.name,
                    command = if (group != null) listOf(group, out.name) else listOf(out.name),
                    name = out.name
                        .replace("-", " ")
                        .replace("_", " ")
                        .replaceFirstChar { it.uppercase() },
                    description = out.about,
                    isCustom = out.isCustom,
                    group = group,
                    args = out.args,
                    api = out.api!!,
                    yamlPath = null,
                ) to out.yamlPath
            }

        return if (fileExists != null) {
            coroutineScope {
                models.map { (action, cliPath) ->
                    async {
                        val path = cliPath?.takeIf { fileExists(it) }
                        action.copy(yamlPath = path)
                    }
                }.awaitAll()
            }
        } else {
            models.map { it.first }
        }
    }

    /**
     * Resolves a query key to a value from IDE sources.
     * Returns null for CLI-resolved inputs (image, clipboard) —
     * the CLI reads them itself.
     */
    private suspend fun resolveQueryValue(queryType: String): String? =
        when (ActionApiInput.fromKey(queryType)) {
            ActionApiInput.Prompt -> getSuspendValue(env.bridge.sys.getDialogText)
            ActionApiInput.FilePath -> getSuspendValue(env.bridge.sys.getCurrentFilePath)
            ActionApiInput.ProjectPath -> getSuspendValue(env.bridge.sys.getProjectPath)
            ActionApiInput.Line -> getSuspendValue(env.bridge.sys.getCursorLine)
            ActionApiInput.Image,
            ActionApiInput.Clipboard,
            ActionApiInput.ClipboardText,
            ActionApiInput.ClipboardPath,
            ActionApiInput.ClipboardImage,
                -> null
            else -> getSuspendValue(env.bridge.sys.getSelectedText)
        }

    /**
     * Executes an action with the query value resolved from IDE sources
     * based on [ActionApi.input] contract and optional [ActionApi.args].
     */
    suspend fun executeAction(
        action: ActionModel,
        onCancel: () -> Unit,
        onSuccess: () -> Unit,
        onEmpty: () -> Unit,
    ) {
        val args = mutableListOf<String>()
        args.addAll(action.command)

        // 1. Resolve api.input → positional arg (skip if null — no input needed)
        val input = action.api.input
        if (input != null) {
            val inputValue = resolveQueryValue(input)
            if (inputValue.isNullOrEmpty() && !ActionApiInput.isResolvedByCli(input)) {
                onCancel.invoke()
                return
            }
            if (!inputValue.isNullOrEmpty()) {
                args.add(inputValue)
            }
        }

        // 2. Resolve api.args → --flag value pairs
        for ((name, queryType) in action.api.args) {
            val value = resolveQueryValue(queryType) ?: continue
            if (value.isEmpty()) continue

            val arg = action.args.find { it.name == name }
            val flag = if (arg?.short != null) "-${arg.short}" else "--$name"

            when (arg?.input) {
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

        logger.d { "Result action: $newCode" }

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
