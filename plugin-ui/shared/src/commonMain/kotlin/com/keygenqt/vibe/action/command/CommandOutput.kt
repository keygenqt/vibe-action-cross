/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Discriminator field used by CLI to distinguish message types.
 */
const val API_DISCRIMINATOR = "export"

/**
 * Default JSON parser without class discriminator, used for general messages.
 */
val commandJson = Json {
    ignoreUnknownKeys = true
}

/**
 * JSON parser with `export` class discriminator for polymorphic `CommandOutput` types.
 */
val commandJsonExport = Json {
    ignoreUnknownKeys = true
    classDiscriminator = API_DISCRIMINATOR
}

/**
 * Envelope wrapping a CLI log message with a level and typed output value.
 */
@Serializable
data class CommandEnvelope(
    val level: String,
    val value: CommandOutput,
)

/**
 * Sealed class representing all possible CLI command output types.
 */
@Serializable
sealed class CommandOutput {
    /**
     * Status output containing version, config, actions count, and actions path.
     */
    @Serializable
    @SerialName("status")
    data class Status(
        @SerialName("total_actions")
        val totalActions: Int,
        @SerialName("total_actions_api")
        val totalActionsApi: Int,
        @SerialName("custom_actions_api")
        val customActionsApi: Int,
        val version: String,
        val config: String,
        @SerialName("actions_path")
        val actionsPath: String,
        @SerialName("config_path")
        val configPath: String,
        @SerialName("cache_path")
        val cachePath: String,
    ) : CommandOutput()

    /**
     * Action description with name, about text, and CLI arguments.
     */
    @Serializable
    @SerialName("actions")
    data class Actions(
        val name: String,
        val about: String,
        val args: List<ActionArg> = emptyList(),
        val api: ActionApi? = null,
        @SerialName("is_custom")
        val isCustom: Boolean = false,
    ) : CommandOutput()

    /**
     * Final successful result containing the generated text/code message.
     */
    @Serializable
    @SerialName("success")
    data class Success(
        val message: String,
    ) : CommandOutput()

    /**
     * Fallback for unrecognized messages, preserves raw `level` and `value`.
     */
    @Serializable
    data class Fallback(
        val level: String,
        val value: JsonObject,
    ) : CommandOutput()

    /**
     * Unknown or unmapped output type.
     */
    @Serializable
    object Unknown : CommandOutput()
}

/**
 * CLI argument definition for an action.
 */
@Serializable
data class ActionArg(
    val name: String,
    val short: String? = null,
    val input: String,
    val help: String? = null,
    val default: String? = null,
)

/**
 * Targets for IDE plugin integration output routing.
 */
@Serializable
enum class ActionApiTarget {
    @SerialName("replace")
    Replace,

    @SerialName("clipboard")
    Clipboard,

    @SerialName("dialog")
    Dialog,
}

/**
 * IDE plugin integration metadata.
 */
@Serializable
data class ActionApi(
    val input: String? = null,
    val output: ActionApiTarget = ActionApiTarget.Replace,
    val args: Map<String, String> = emptyMap(),
)
