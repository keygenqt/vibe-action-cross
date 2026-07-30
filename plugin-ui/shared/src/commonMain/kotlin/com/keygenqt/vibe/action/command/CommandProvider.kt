/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.command

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.bridge.Environment
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds

/**
 * Provider that executes CLI commands and returns parsed output.
 */
class CommandProvider(
    private val env: Environment,
    private val logger: Logger,
) {
    private val runCli get() = env.bridge.sys.runCli ?: error("runCli is not available")

    /**
     * Executes 'status' command and returns list of outputs.
     */
    suspend fun status(): List<CommandOutput> = execute(listOf("status"))

    /**
     * Executes default command (actions list) and returns parsed outputs.
     */
    suspend fun actions(): List<CommandOutput> = execute(listOf())

    /**
     * Executes 'stop' command to terminate running CLI processes.
     */
    suspend fun stop(): List<CommandOutput> = execute(listOf("stop"))

    /**
     * General command execution, suspending until CLI process finishes.
     *
     * Cancellation is handled via coroutine cancellation. If a newer command
     * is started by the ViewModel (which cancels the previous Job), the
     * continuation becomes inactive, and the result of the lingering process
     * is safely discarded.
     */
    suspend fun execute(args: List<String>): List<CommandOutput> {
        try {
            return withTimeout(CLI_TIMEOUT) {
                suspendCancellableCoroutine { cont ->
                    val result = mutableListOf<CommandOutput>()
                    try {
                        runCli(
                            args,
                            { line -> result.add(parseCommandOutputLine(line)) },
                            { exitCode ->
                                if (exitCode == 0) {
                                    // Check isActive to avoid resuming a cancelled continuation
                                    if (cont.isActive) cont.resume(result.toList())
                                } else {
                                    if (cont.isActive) {
                                        cont.resumeWithException(
                                            RuntimeException(
                                                "Command ${args.ifEmpty { listOf("default") }.joinToString(" ")} " +
                                                    "failed with exit code $exitCode",
                                            ),
                                        )
                                    }
                                }
                            },
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Platform bridge may throw synchronously (e.g. IntelliJ throws
                        // ExecutionException when the CLI binary is not found in PATH).
                        // Resume here — the outer resumeWithException would throw
                        // IllegalStateException on an already-resumed continuation.
                        if (cont.isActive) cont.resumeWithException(e)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            // Coroutine is canceled here; the lingering CLI process is cleaned up
            // on the Rust side — the next CLI launch terminates the previous one
            // via the PID-file mechanism, so stale processes don't accumulate.
            throw RuntimeException(
                "Command ${args.ifEmpty { listOf("default") }.joinToString(" ")} " +
                    "timed out after ${CLI_TIMEOUT}s",
                e,
            )
        }
    }

    /**
     * Parses a single line of CLI output.
     * Uses the presence of the discriminator key inside the "value" object
     * to decide between typed (Status/Actions) and untyped (Fallback) decoding.
     */
    private fun parseCommandOutputLine(line: String): CommandOutput {
        // First-pass parse – if the line is not a valid JSON object, treat as Unknown
        val obj = runCatching { commandJson.parseToJsonElement(line).jsonObject }.getOrNull()
            ?: return CommandOutput.Unknown

        // Discriminator lives *inside* the "value" key: {"level":"info","value":{"export":"status",...}}
        val hasDiscriminator = (obj["value"] as? JsonObject)?.containsKey(API_DISCRIMINATOR) == true

        if (hasDiscriminator) {
            runCatching { return commandJsonExport.decodeFromJsonElement(CommandEnvelope.serializer(), obj).value }
                .onFailure { logger.w(it) { "Discriminator present but decode failed: $line" } }
        }

        // Fallback for logs, usage text, or any other non-discriminated output
        return runCatching { commandJson.decodeFromJsonElement(CommandOutput.Fallback.serializer(), obj) }
            .getOrElse {
                logger.w(it) { "Failed to parse line: $line" }
                CommandOutput.Unknown
            }
    }

    private companion object {
        val CLI_TIMEOUT = 30.seconds
    }
}
