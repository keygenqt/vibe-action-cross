package com.keygenqt.vibe.action.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Discriminator field used by CLI to distinguish message types.
 */
const val apiDiscriminator = "export"

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
    classDiscriminator = apiDiscriminator
}

/**
 * Envelope wrapping a CLI log message with a level and typed output value.
 */
@Serializable
data class CommandEnvelope(
    val level: String,
    val value: CommandOutput
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
        val actions: String,
        val version: String,
        val config: String,
        @SerialName("actions_path")
        val actionsPath: String,
    ) : CommandOutput()

    /**
     * Action description with name and about text.
     */
    @Serializable
    @SerialName("actions")
    data class Actions(
        val name: String,
        val about: String,
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
